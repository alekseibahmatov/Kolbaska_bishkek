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

    public CustomerInformationResponse getWaiter(Long id) {
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
                .deleted(user.getDeleted())
                .activationCode(user.getActivationCode())
                .restaurantId(user.getRestaurant() == null ? -1 : user.getRestaurant().getId())
                .transactions(TransactionMapper.INSTANCE.toTransactionResponseList(user.getTransactions()))
                .logins(LoginMapper.INSTANCE.toLoginResponse(user.getLogins().stream()))
                .roleNames(userConfiguration.getRoleNames(user));

        return response.build();
    }

    public CustomerUpdateResponse updateWaiter(AdminCustomerUpdateRequest request) throws RoleNotFoundException {
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

        return CustomerUpdateResponse.builder()
                .message("User was successfully updated!")
                .build();
    }

    static void updateWaiterImpl(User user, String fullName, String newPassword, PasswordEncoder passwordEncoder, FormatService formatService, String phone, String personalCode, AddressRequest address, AddressRepository addressRepository, String email) {
        user.setFullName(fullName);

        if (newPassword != null) user.setPassword(passwordEncoder.encode(newPassword));

        user.setPhone(formatService.formatE164(phone));
        user.setPersonalCode(personalCode);

        Address userAddress = user.getAddress();
        userAddress.setCity(address.getCity());
        userAddress.setCountry(address.getCountry());
        userAddress.setState(address.getState());
        userAddress.setApartmentNumber(address.getApartmentNumber());
        userAddress.setZipCode(address.getZipCode());

        addressRepository.save(userAddress);

        user.setEmail(email);
    }

    public List<WaiterResponse> getWaiters() {
        List<User> waiters = userRepository.findAllWaiters();

        List<WaiterResponse> responses = new ArrayList<>();

        for (User waiter : waiters) {
            WaiterResponse waiterResponse = new WaiterResponse();

            waiterResponse.setId(waiter.getId());
            waiterResponse.setFullName(waiter.getFullName());
            waiterResponse.setEmail(waiter.getEmail());
            waiterResponse.setPhone(waiter.getPhone());

            double turnover = 0.0;

            for (Transaction t: waiter.getTransactions()) {
                turnover += t.getValue();
            }

            waiterResponse.setTurnover(turnover);

            responses.add(waiterResponse);
        }

        return responses;
    }
}
