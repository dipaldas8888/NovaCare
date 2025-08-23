package com.dipal.NovaCare.security;

import com.dipal.NovaCare.model.User;
import com.dipal.NovaCare.repository.UserRepository; // make sure you have this repo
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository; // inject via SecurityConfig (see step 3)

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String idToken = header.substring(7);
        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);

            // 1) Find your app user by Firebase UID (or email if you prefer)
            User user = userRepository.findByFirebaseUid(decoded.getUid())
                    .orElseGet(() ->
                            userRepository.findByEmail(decoded.getEmail())
                                    .orElse(null)
                    );

            if (user == null) {
                // If user not present in DB, block or create-on-first-login (your choice)
                log.warn("Firebase user authenticated but not found in DB. uid={}, email={}", decoded.getUid(), decoded.getEmail());
                chain.doFilter(request, response);
                return;
            }

            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))

                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user.getFirebaseUid(), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (FirebaseAuthException e) {
            log.error("Invalid Firebase token", e);

        }

        chain.doFilter(request, response);
    }
}
