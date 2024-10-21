const path = $("script[src*=nav]").attr("src")
const dir = path.replace("nav.js", "")

console.log("Test")


// $(document).ready(function () {
$.get(dir + "nav.html", {async: false}, function (data) {
    console.log("loaded")
    console.log(data)
    $("#navigation").html(data);
})
console.log("Done")
// })


