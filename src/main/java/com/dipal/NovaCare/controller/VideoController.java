package com.dipal.NovaCare.controller;

import com.dipal.NovaCare.service.AppointmentService;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VideoGrant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.apiKeySid}")
    private String apiKeySid;

    @Value("${twilio.apiKeySecret}")
    private String apiKeySecret;

    private final AppointmentService appointmentService;

    public VideoController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Generate Twilio Video token for a user to join a room (appointment)
     * @param identity unique user identity (e.g. "doctor123" or "patient456")
     * @param roomName appointment ID or unique room name
     */
    @GetMapping("/token")
    public ResponseEntity<?> generateToken(@RequestParam String identity,
                                           @RequestParam String roomName,
                                           Authentication authentication) {
        // 1. Check if user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String loggedInUserEmail = authentication.getName();

        // 2. Check if logged-in user is authorized to join this appointment room
        boolean allowed = appointmentService.isUserInAppointment(loggedInUserEmail, roomName);
        if (!allowed) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        // 3. Generate Twilio Video token
        VideoGrant grant = new VideoGrant();
        grant.setRoom(roomName);

        AccessToken token = new AccessToken.Builder(accountSid, apiKeySid, apiKeySecret)
                .identity(identity)
                .grant(grant)
                .build();

        return ResponseEntity.ok(token.toJwt());
    }
}
