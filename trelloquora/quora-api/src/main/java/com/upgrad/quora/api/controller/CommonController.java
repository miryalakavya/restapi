package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class CommonController
{

    @Autowired
    UserBusinessService userBusinessService;

    /**
     *  Handles /userprofile/{userId} endpoint which is used to get the details of any user in the Quora Application.
     *  This endpoint can be accessed by any user in the application
     * @param PathVariable("userId")
     * @param RequestHeader("authorization")
     * @return ResponseEntity<UserDetailsResponse>
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET, path = "/userprofile/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> userProfile(@PathVariable("userId") final String Uuid, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, UserNotFoundException {

        final UserEntity userEntity = userBusinessService.getUserProfile(Uuid, authorization);

        UserDetailsResponse userDetailsResponse = new UserDetailsResponse().userName(userEntity.getUsername())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .emailAddress(userEntity.getEmail())
                .country(userEntity.getCountry())
                .dob(userEntity.getDob())
                .aboutMe(userEntity.getAboutme())
                .contactNumber(userEntity.getContactnumber());

        return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);
    }
}

