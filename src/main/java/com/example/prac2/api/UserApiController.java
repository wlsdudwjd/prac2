package com.example.prac2.api;

import com.example.prac2.model.SimpleUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
public class UserApiController {

    private final Map<Long, SimpleUser> store = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong(1);

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<SimpleUser>> createUser(@RequestBody CreateUserRequest request) {
        if (isBlank(request.name()) || isBlank(request.email())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("name and email are required"));
        }
        SimpleUser user = new SimpleUser(ids.getAndIncrement(), request.name().trim(), request.email().trim(), true);
        store.put(user.getId(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(user));
    }

    @PostMapping("/users/maintenance")
    public ResponseEntity<ApiResponse<Object>> maintenanceMode() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("User service is temporarily unavailable"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<SimpleUser>>> listUsers() {
        List<SimpleUser> users = new ArrayList<>(store.values());
        Collections.sort(users, (a, b) -> a.getId().compareTo(b.getId()));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<SimpleUser>> getUser(@PathVariable Long id) {
        SimpleUser user = store.get(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User " + id + " not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<SimpleUser>> updateUser(@PathVariable Long id,
                                                              @RequestBody UpdateUserRequest request) {
        SimpleUser existing = store.get(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User " + id + " not found"));
        }
        if (isBlank(request.name()) || isBlank(request.email())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("name and email are required"));
        }
        existing.setName(request.name().trim());
        existing.setEmail(request.email().trim());
        return ResponseEntity.ok(ApiResponse.success(existing));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<SimpleUser>> updateStatus(@PathVariable Long id,
                                                                @RequestBody StatusUpdateRequest request) {
        SimpleUser existing = store.get(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User " + id + " not found"));
        }
        if ("panic".equalsIgnoreCase(request.reason())) {
            throw new IllegalStateException("Manual failure requested for testing 500");
        }
        existing.setActive(request.active());
        return ResponseEntity.ok(ApiResponse.success(existing));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
        SimpleUser removed = store.remove(id);
        if (removed == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User " + id + " not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("deletedId", id)));
    }

    @DeleteMapping("/users/inactive")
    public ResponseEntity<ApiResponse<Object>> deleteInactiveUsers() {
        List<Long> inactiveIds = store.values().stream()
                .filter(user -> !user.isActive())
                .map(SimpleUser::getId)
                .toList();
        if (inactiveIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No inactive users to delete"));
        }
        inactiveIds.forEach(store::remove);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "removedCount", inactiveIds.size(),
                "ids", inactiveIds
        )));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record CreateUserRequest(String name, String email) {
    }

    public record UpdateUserRequest(String name, String email) {
    }

    public record StatusUpdateRequest(boolean active, String reason) {
    }
}
