import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

void main() {
    FactoryOptimizer.main();
}

public static class FactoryOptimizer {

    public static void main() {
        List<Recipe> recipes = loadRecipesFromCSV("recipes.csv");
        if (!recipes.isEmpty()) {
            optimizeFactory(recipes);
        }
    }

    public static List<Recipe> loadRecipesFromCSV(String filePath) {
        List<Recipe> recipes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                Recipe recipe = new Recipe(parts[0].trim());

                // Inputs parsen
                if (!parts[1].trim().isEmpty()) {
                    for (String pair : parts[1].split(";")) {
                        String[] kv = pair.split(":");
                        recipe.inputs.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
                    }
                }

                // Outputs parsen
                if (!parts[2].trim().isEmpty()) {
                    for (String pair : parts[2].split(";")) {
                        String[] kv = pair.split(":");
                        recipe.outputs.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
                    }
                }

                recipes.add(recipe);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recipes;
    }

    public static void optimizeFactory(List<Recipe> recipes) {
        Set<String> allItems = new HashSet<>();
        for (Recipe r : recipes) {
            allItems.addAll(r.inputs.keySet());
            allItems.addAll(r.outputs.keySet());
        }
        allItems.add("e"); // Strom erzwingen

        List<String> itemList = new ArrayList<>(allItems);
        int numRecipes = recipes.size();
        int numItems = itemList.size();

        double[][] matrix = new double[numItems][numRecipes];
        double[] powerConsumed = new double[numRecipes];
        double[] powerProduced = new double[numRecipes];

        for (int j = 0; j < numRecipes; j++) {
            Recipe r = recipes.get(j);
            powerConsumed[j] = r.inputs.getOrDefault("e", 0.0);
            powerProduced[j] = r.outputs.getOrDefault("e", 0.0);

            for (int i = 0; i < numItems; i++) {
                String item = itemList.get(i);
                if (item.equals("e")) {
                    matrix[i][j] = 0.0;
                    continue;
                }
                double produced = r.outputs.getOrDefault(item, 0.0);
                double consumed = r.inputs.getOrDefault(item, 0.0);
                matrix[i][j] = produced - consumed;
            }
        }

        // Zielfunktion: Minimiere Stromverbrauch für feste Stromproduktion = 1.0
        LinearObjectiveFunction objective = new LinearObjectiveFunction(powerConsumed, 0);
        List<LinearConstraint> constraints = new ArrayList<>();

        System.out.println("=== ITEM-KLASSIFIZIERUNG ===");
        for (int i = 0; i < numItems; i++) {
            String item = itemList.get(i);
            if (item.equals("e")) continue;

            // Regel: Ein Item ist NUR DANN ein Basis-Rohstoff, wenn es in KEINEM Output vorkommt.
            boolean isProduced = recipes.stream().anyMatch(r -> r.outputs.containsKey(item));
            boolean isConsumed = recipes.stream().anyMatch(r -> r.inputs.containsKey(item));

            if (isProduced) {
                // Es wird irgendwo hergestellt -> Craftable / Intermediate Product
                if (isConsumed) {
                    constraints.add(new LinearConstraint(matrix[i], Relationship.EQ, 0.0));
                } else {
                    // Reines Abfallprodukt (wird produziert, aber von keinem Rezept verbraucht)
                    constraints.add(new LinearConstraint(matrix[i], Relationship.GEQ, 0.0));
                }
                // System.out.println("  [Zwischenprodukt] " + item + " -> Streng balanciert (= 0)");
            } else {
                // Es wird von KEINEM Rezept hergestellt -> Echte Basisressource (Raw Material)
                System.out.println("  [Basis-Rohstoff]  " + item + " -> Darf von außen bezogen werden");
            }
        }

        // Normierung: Gesamte Stromproduktion muss exakt 1.0 sein
        constraints.add(new LinearConstraint(powerProduced, Relationship.EQ, 1.0));

        try {
            SimplexSolver solver = new SimplexSolver();
            PointValuePair solution = solver.optimize(
                    objective,
                    new LinearConstraintSet(constraints),
                    GoalType.MINIMIZE,
                    new NonNegativeConstraint(true)
            );

            double minConsumption = solution.getValue();
            double efficiency = 1.0 - minConsumption;

            System.out.println("\n=== OPTIMALES ERGEBNIS ===");
            System.out.printf("Maximale Effizienz: %.2f%%\n", efficiency * 100.0);
            System.out.println("\nAktive Maschinen-Multiplikatoren (für 1.0 Stromproduktion):");

            double[] point = solution.getPoint();
            for (int j = 0; j < numRecipes; j++) {
                if (point[j] != 0) {
                    System.out.printf("  - %s: %.9f\n", recipes.get(j).name, point[j]);
                }
            }

        } catch (Exception e) {
            System.out.println("Optimierung fehlgeschlagen: " + e.getMessage());
        }
    }

    public static class Recipe {
        String name;
        Map<String, Double> inputs = new HashMap<>();
        Map<String, Double> outputs = new HashMap<>();

        public Recipe(String name) {
            this.name = name;
        }
    }
}