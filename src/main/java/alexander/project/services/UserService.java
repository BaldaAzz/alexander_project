package alexander.project.services;

import alexander.project.models.User;

public interface UserService {
    User registerNewUser(User user);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    User updateProfile(Long userId, String firstName, String lastName, String email, String phone);
    void changePassword(Long userId, String currentPassword, String newPassword);
    void updateNotificationSettings(Long userId, boolean emailNotifications, boolean transactionNotifications, boolean balanceNotifications);
    void updateCurrency(Long userId, String currency);
    void deleteAccount(Long userId, String password);
}
