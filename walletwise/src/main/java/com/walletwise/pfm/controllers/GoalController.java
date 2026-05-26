package com.walletwise.pfm.controllers;

import com.walletwise.pfm.dto.request.GoalRequest;
import com.walletwise.pfm.dto.request.UpdateGoalRequest;
import com.walletwise.pfm.dto.response.GoalResponse;
import com.walletwise.pfm.services.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    /** POST /api/goals */
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody GoalRequest req) {
        GoalResponse goal = goalService.createGoal(userDetails.getUsername(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(goal);
    }

    /** GET /api/goals */
    @GetMapping
    public ResponseEntity<Map<String, List<GoalResponse>>> getAllGoals(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of("goals", goalService.getAllGoals(userDetails.getUsername())));
    }

    /** GET /api/goals/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(goalService.getGoal(userDetails.getUsername(), id));
    }

    /** PUT /api/goals/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateGoalRequest req) {
        return ResponseEntity.ok(goalService.updateGoal(userDetails.getUsername(), id, req));
    }

    /** DELETE /api/goals/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        goalService.deleteGoal(userDetails.getUsername(), id);
        return ResponseEntity.ok(Map.of("message", "Goal deleted successfully"));
    }
}
