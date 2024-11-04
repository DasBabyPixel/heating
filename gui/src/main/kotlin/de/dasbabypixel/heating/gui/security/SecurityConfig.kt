package de.dasbabypixel.heating.gui.security

import com.vaadin.flow.spring.security.AuthenticationContext
import com.vaadin.flow.spring.security.VaadinWebSecurity
import de.dasbabypixel.heating.gui.LoginGui
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.stereotype.Component

@EnableWebSecurity
@Configuration
class SecurityConfig : VaadinWebSecurity() {

    override fun configure(http: HttpSecurity) {
        super.configure(http)
        setLoginView(http, LoginGui::class.java)
    }

    @Bean
    fun users(): UserDetailsService {
        val user = User.builder()
            .username("user")
            .password("{bcrypt}\$2a\$12\$BhfjUwjfaRjoe/6Lw.gV.OUaguQN7Dns1R5UghUgl69aafyD72JSW")
            .roles("USER")
            .build()
        val admin = User.builder()
            .username("admin")
            .password("{bcrypt}\$2a\$12\$BhfjUwjfaRjoe/6Lw.gV.OUaguQN7Dns1R5UghUgl69aafyD72JSW")
            .roles("USER", "ADMIN")
            .build()
        return InMemoryUserDetailsManager(user, admin) // return InMemoryUserDetailsManager(user)
    }
}

@Component
class SecurityService(private val authenticationContext: AuthenticationContext) {
    val authenticatedUser: UserDetails
        get() = authenticationContext.getAuthenticatedUser(UserDetails::class.java).get()

    fun logout() {
        authenticationContext.logout()
    }
}