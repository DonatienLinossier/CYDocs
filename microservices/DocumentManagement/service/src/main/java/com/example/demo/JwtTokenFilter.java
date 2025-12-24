public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil; // Utilité pour valider et parser le token
    // ...

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {

        // 1. Extraire l'en-tête Authorization
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Extraire le token
        final String token = header.substring(7);

        // 3. Valider le token et l'utilisateur
        if (!jwtTokenUtil.validate(token)) {
            chain.doFilter(request, response);
            return;
        }

        // 4. Mettre l'utilisateur dans le contexte de sécurité
        // Ici, vous récupérez l'ID de l'utilisateur (ou le username) du token
        String username = jwtTokenUtil.getUsernameFromToken(token);
        
        UserDetails userDetails = // ... charger l'utilisateur par son nom/ID ...

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }
}