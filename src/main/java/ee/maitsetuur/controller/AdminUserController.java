package ee.maitsetuur.controller;

import ee.maitsetuur.exception.UserAlreadyExistsException;
import ee.maitsetuur.request.AdminCustomerUpdateRequest;
import ee.maitsetuur.request.UserCreationRequest;
import ee.maitsetuur.response.CustomerInformationResponse;
import ee.maitsetuur.response.CustomerUpdateResponse;
import ee.maitsetuur.response.UserResponse;
import ee.maitsetuur.service.AdminUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RoleNotFoundException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.basepath}/admin")
public class AdminUserController {

    private final AdminUserService service;

    @GetMapping("/user")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(service.getUsers());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<CustomerInformationResponse> getUser(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getUser(id));
    }

    @PostMapping("/user")
    public ResponseEntity<UserResponse> createUser(
            @Valid @NotNull UserCreationRequest request
    ) throws RoleNotFoundException, UserAlreadyExistsException {
        return ResponseEntity.ok(service.createUser(request));
    }

    @PutMapping("/user")
    public ResponseEntity<CustomerUpdateResponse> updateUser(
         @Valid @NotNull AdminCustomerUpdateRequest request
    ) throws RoleNotFoundException {
        return ResponseEntity.ok(service.updateUser(request));
    }
}
