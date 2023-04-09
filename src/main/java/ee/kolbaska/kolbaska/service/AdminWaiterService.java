package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.config.UserConfiguration;
import ee.kolbaska.kolbaska.mapper.AddressMapper;
import ee.kolbaska.kolbaska.mapper.LoginMapper;
import ee.kolbaska.kolbaska.mapper.TransactionMapper;
import ee.kolbaska.kolbaska.model.address.Address;
import ee.kolbaska.kolbaska.model.transaction.Transaction;
import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.AddressRepository;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.AddressRequest;
import ee.kolbaska.kolbaska.request.AdminCustomerUpdateRequest;
import ee.kolbaska.kolbaska.response.CustomerInformationResponse;
import ee.kolbaska.kolbaska.response.CustomerUpdateResponse;
import ee.kolbaska.kolbaska.response.WaiterResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.FormatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminWaiterService {

    private final UserConfiguration userConfiguration;

    private final PasswordEncoder passwordEncoder;

    private final FormatService formatService;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final AddressRepository addressRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminWaiterService.class);

    public CustomerInformationResponse getWaiter(Long id) {
        LOGGER.info("Getting information for waiter with id {}", id);

        Optional<User> ifUser = userRepository.findById(id);

        if (ifUser.isEmpty()) throw new UsernameNotFoundException("Waiter with such id wasn't found");

        User user = ifUser.get();

        CustomerInformationResponse.CustomerInformationResponseBuilder response = CustomerInformationResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(AddressMapper.INSTANCE.toAddressResponse(user.getAddress()))
                .personalCode(user.getPersonalCode())
                .activated(user.getActivated())
                .deleted(user.isDeleted())
                .activationCode(user.getActivationCode())
                .restaurantCode(user.getRestaurant() == null ? "" : user.getRestaurant().getRestaurantCode())
                .transactions(TransactionMapper.INSTANCE.toTransactionResponseList(user.getTransactions()))
                .logins(LoginMapper.INSTANCE.toLoginResponse(user.getLogins().stream()))
                .roleNames(userConfiguration.getRoleNames(user));

        LOGGER.info("Returning information for waiter with id {}: {}", id, response.build());

        return response.build();
    }

    public CustomerUpdateResponse updateWaiter(AdminCustomerUpdateRequest request) throws RoleNotFoundException {
        LOGGER.info("Updating waiter with request {}", request);

        Optional<User> ifUser = userRepository.findById(request.getId());

        if (ifUser.isEmpty()) throw new UsernameNotFoundException("Waiter with such id wasn't found");

        User user = ifUser.get();

        updateWaiterImpl(user, request.getFullName(), request.getNewPassword(), passwordEncoder, formatService, request.getPhone(), request.getPersonalCode(), request.getAddress(), addressRepository, request.getEmail());
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

        CustomerUpdateResponse response = CustomerUpdateResponse.builder()
                .message("User was successfully updated!")
                .build();

        LOGGER.info("Waiter updated: {}", response);

        return response;
    }

    static void updateWaiterImpl(User user, String fullName, String newPassword, PasswordEncoder passwordEncoder, FormatService formatService, String phone, String personalCode, AddressRequest address, AddressRepository addressRepository, String email) {
        LOGGER.info("Updating waiter {} with fullName = {}, phone = {}, personalCode = {}, email = {}", user.getId(), fullName, phone, personalCode, email);

        user.setFullName(fullName);

        if (newPassword != null && !newPassword.equals("")) user.setPassword(passwordEncoder.encode(newPassword));

        user.setPhone(formatService.formatE164(phone));
        user.setPersonalCode(personalCode);

        Address userAddress = AddressMapper.INSTANCE.toAddress(address);

        addressRepository.save(userAddress);

        user.setEmail(email);

        LOGGER.info("Waiter updated: {}", user);
    }

    public List<WaiterResponse> getWaiters() {
        LOGGER.info("Getting list of waiters");

        List<User> waiters = userRepository.findUsersByRestaurantIsNotNull();

        List<WaiterResponse> responses = new ArrayList<>();

        for (User waiter : waiters) {
            WaiterResponse waiterResponse = new WaiterResponse();

            waiterResponse.setId(waiter.getId());
            waiterResponse.setFullName(waiter.getFullName());
            waiterResponse.setEmail(waiter.getEmail());
            waiterResponse.setPhone(waiter.getPhone());

            double turnover;

            if (waiter.getTransactions() != null) {
                turnover = waiter.getTransactions().stream()
                        .mapToDouble(Transaction::getValue)
                        .sum();
            } else turnover = 0.0;

            waiterResponse.setTurnover(turnover);

            responses.add(waiterResponse);
        }
        LOGGER.info("Returning list of waiters: {}", responses);

        return responses;
    }
}
