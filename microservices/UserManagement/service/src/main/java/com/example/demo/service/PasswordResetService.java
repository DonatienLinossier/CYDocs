@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    public boolean sendResetToken(String email) {

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            userService.logger().warn("Utilisateur introuvable : " + email);
            return false;
        }

        String token = tokenService.createResetToken(user);

        String resetLink = "https://l'adresse_du_front/reset-password?token=" + token;

        emailService.send(
            email,
            "Réinitialisation de votre mot de passe",
            "Bonjour,\n\n" +
            "Voici votre lien pour réinitialiser votre mot de passe :\n" +
            resetLink + "\n\n" +
            "Si vous n’êtes pas à l’origine de cette demande, ignorez cet e-mail."
        );

        userService.logger().info("Token envoyé à " + email);
        return true;
    }

    public boolean resetPassword(String tokenValue, String newPassword) {

        Long userId = tokenService.validate(tokenValue, "RESET");
        if (userId == null) {
            userService.logger().warn("Token invalide ou expiré : " + tokenValue);
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            userService.logger().warn("Aucun utilisateur pour ce token");
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenService.invalidate(tokenValue);

        userService.logger().info("Mot de passe réinitialisé pour : " + user.getEmail());
        return true;
    }
}
