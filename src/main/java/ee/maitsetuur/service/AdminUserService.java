package ee.maitsetuur.service;

import ee.maitsetuur.config.UserConfiguration;
import ee.maitsetuur.exception.UserAlreadyExistsException;
import ee.maitsetuur.mapper.AddressMapper;
import ee.maitsetuur.mapper.AddressMapperImpl;
import ee.maitsetuur.mapper.LoginMapper;
import ee.maitsetuur.mapper.TransactionMapper;
import ee.maitsetuur.model.address.Address;
import ee.maitsetuur.model.user.Role;
import ee.maitsetuur.model.user.User;
import ee.maitsetuur.repository.AddressRepository;
import ee.maitsetuur.repository.RoleRepository;
import ee.maitsetuur.repository.UserRepository;
import ee.maitsetuur.request.AddressRequest;
import ee.maitsetuur.request.AdminCustomerUpdateRequest;
import ee.maitsetuur.request.UserCreationRequest;
import ee.maitsetuur.response.CustomerInformationResponse;
import ee.maitsetuur.response.CustomerUpdateResponse;
import ee.maitsetuur.response.UserResponse;
import ee.maitsetuur.service.miscellaneous.FormatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserConfiguration userConfig;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final AddressRepository addressRepository;

    private final FormatService formatService;

    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getUsers() {

        List<User> users = userRepository.findAll();

        List<UserResponse> response = new ArrayList<>();

        for (User u : users) {
            List<String> userRoles = userConfig.getRoleNames(u);

            UserResponse userResponse = UserResponse.builder()
                    .id(u.getId())
                    .fullName(u.getFullName())
                    .email(u.getEmail())
                    .roleName(userRoles.get(0))
                    .restaurantName(userRoles.contains("ROLE_MANAGER") ? u.getManagedRestaurant().getName() : u.getRestaurant().getName())
                    .build();

            response.add(userResponse);
        }

        return response;
    }

    public CustomerInformationResponse getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return CustomerInformationResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(AddressMapper.INSTANCE.toAddressResponse(user.getAddress()))
                .personalCode(user.getPersonalCode())
                .activated(user.getActivated())
                .deleted(user.isDeleted())
                .activationCode(user.getActivationCode())
                .transactions(TransactionMapper.INSTANCE.toTransactionResponseList(user.getTransactions()))
                .logins(LoginMapper.INSTANCE.toLoginResponse(user.getLogins().stream()))
                .roleNames(userConfig.getRoleNames(user))
                .build();
    }

    public UserResponse createUser(UserCreationRequest request) throws UserAlreadyExistsException, RoleNotFoundException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) throw new UserAlreadyExistsException("User with this email is already exists");

        Address newAddress = AddressMapperImpl.INSTANCE.toAddress(request.getAddress());

        newAddress = addressRepository.save(newAddress);

        Role userRole = roleRepository.findRoleByRoleName(request.getRoleName()).orElseThrow(() -> new RoleNotFoundException("Role you tried to add to current user does not exists"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .personalCode(request.getPersonalCode())
                .activated(true)
                .address(newAddress)
                .roles(List.of(userRole))
                .build();

        user = userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .restaurantName(null)
                .roleName(request.getRoleName())
                .build();
    }

    public CustomerUpdateResponse updateUser(AdminCustomerUpdateRequest request) throws RoleNotFoundException {
        User user = userRepository.findById(request.getId()).orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        updateUserImpl(user, request.getFullName(), request.getNewPassword(), passwordEncoder, formatService, request.getPhone(), request.getPersonalCode(), request.getAddress(), addressRepository, request.getEmail());
        user.setActivated(request.getActivated());
        user.setDeleted(request.getDeleted());
        user.setActivationCode(request.getActivationCode());

        List<Role> userRoles = new ArrayList<>();

        for (String roleName : request.getRoleNames()) {
            Optional<Role> role = roleRepository.findRoleByRoleName(roleName);

            if (role.isEmpty()) throw new RoleNotFoundException("Role that you trying to assign wasn't not found");

            userRoles.add(role.get());
        }

        user.setRoles(userRoles);

        user.setActivationCode(request.getActivationCode());

        userRepository.save(user);

        return CustomerUpdateResponse.builder()
                .message("User was successfully updated!")
                .build();

    }

    static void updateUserImpl(User user, String fullName, String newPassword, PasswordEncoder passwordEncoder, FormatService formatService, String phone, String personalCode, AddressRequest address, AddressRepository addressRepository, String email) {
        user.setFullName(fullName);

        if (newPassword != null && !newPassword.equals("")) user.setPassword(passwordEncoder.encode(newPassword));

        user.setPhone(formatService.formatE164(phone));
        user.setPersonalCode(personalCode);

        Address userAddress = AddressMapper.INSTANCE.toAddress(address);

        addressRepository.save(userAddress);

        user.setEmail(email);
    }
}
