/* Copyright (C) [2024-2026], INESC TEC
 *
 * dataspace-audit-logger
 * Implements an audit logger and Clearing House in accordance with thethe International Data Space Reference Architecture.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 * CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations 
 * under the License.
 *
 * You can reach INESC TEC Technology Technology Transfer Office (TTO) at tech-transfer@inesctec.pt, or
 * Campus da Faculdade de Engenharia da Universidade do Porto
 * Rua Dr. Roberto Frias
 * 4200-465 Porto
 * Portugal 
 */
package pt.inesctec.ids.clearinghouse;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig{


  private final Environment environment;

  public SecurityConfig(Environment environment) {
    this.environment = environment;
  }

// Old version replaced on 07/01-2026 (app version 1.1.3) 
//  @Bean
//  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    http
//        .authorizeRequests() // allow requests
//        .antMatchers("/", "/api/ids/data", "/health", "/messages", "/messages/**", "/process/**").permitAll() // that matches this
//        .anyRequest() // all requests above
//        .authenticated()  // needs authentication
//        .and()
//        .csrf().disable()
//        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//        .httpBasic();
//    return http.build();
//  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
    .csrf(csrf -> csrf.disable())
    .authorizeRequests((authz) -> authz
      // /keep-alive endpoint is non secured
      .antMatchers("/", "/api/ids/data", "/health", "/messages", "/messages/**", "/process/**").permitAll()
      // matcher rules for "/api/**"
      .antMatchers(HttpMethod.POST,   "/api/provided-edi-artifacts/update/**").hasAnyAuthority( "read", "write")
      .antMatchers(HttpMethod.GET,    "/api/provided-edi-artifacts/all/**").hasAnyAuthority( "read", "write")
      .antMatchers(HttpMethod.GET,    "/api/provided-edi-artifacts/from-participant/**").hasAnyAuthority( "read", "write")
      .antMatchers(HttpMethod.DELETE, "/api/provided-edi-artifacts/from-participant/**").hasAnyAuthority( "write")
      .antMatchers(HttpMethod.POST,   "/api/consumed-edi-artifacts/**").hasAnyAuthority( "read", "write")
      .antMatchers(HttpMethod.GET,    "/api/consumed-edi-artifacts/all/**").hasAnyAuthority( "read", "write")
      .antMatchers(HttpMethod.GET,    "/api/consumed-edi-artifacts/to-participant/**").hasAnyAuthority( "read", "write")
      .antMatchers(HttpMethod.DELETE, "/api/consumed-edi-artifacts/to-participant/**").hasAnyAuthority( "write")
      .anyRequest().authenticated())
    .httpBasic(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

    // "spring.security.user.name" user with read permissions
    UserDetails user = User.withUsername(environment.getProperty("spring.security.user.name"))
      .password(passwordEncoder.encode(environment.getProperty("spring.security.user.password")))
      .roles("USER")
      .authorities("read")
      .build();
    manager.createUser(user);
    
    // users specified in the appl properties file with the declared permissions
    for (UserDetails user1 : getUserDetailsFromAppProperties(passwordEncoder)) {
      manager.createUser(user1);
    }

    return manager;
  }

  // Get user description from the application.properties
  private List<UserDetails> getUserDetailsFromAppProperties(PasswordEncoder passwordEncoder) {
    var list = new ArrayList<UserDetails>();

    for (int i = 1; true; ++i) {
      String name = String.format("Broker.user%s.name", i);
      String passwd = String.format("Broker.user%s.password", i);
      String auths = String.format("Broker.user%s.authorities", i);
      String userName = environment.getProperty(name);
      String userPasswd = environment.getProperty(passwd);
      String userAuths = environment.getProperty(auths);

      if (userName != null && userPasswd != null && userAuths != null) {
        UserDetails user = User
          .withUsername(userName)
          .password(passwordEncoder.encode(userPasswd))
          .roles("USER")
          .authorities(userAuths.split(","))
          .build();
         list.add(user);
      }
      else
        break;
    }

    return list;
  }  
  
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
