package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SigninResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserBusinessService userBusinessService;

  /**
   * Handles "/user/signup" endpoint which is used to register a new user in the Quora Application.
   *
   * @param SignupUserRequest
   * @return ResponseEntity<SignupUserResponse>
   * @throws SignUpRestrictedException
   */
  @RequestMapping(
      method = RequestMethod.POST,
      path = "/signup",
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<SignupUserResponse> signup(final SignupUserRequest signupUserRequest)
      throws SignUpRestrictedException {

        //Fill in the UserEntity object with the information received on SignupUserRequest
        final UserEntity userEntity = new UserEntity();
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setUsername(signupUserRequest.getUserName());
        userEntity.setEmail(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());

        // fill in a dummy salt string for now, later this will be replaced with actual salt
        userEntity.setSalt("salt");
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setAboutme(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());

        // by default all users created through the endpoint are non-admin users
        userEntity.setRole("nonadmin"); //default nonadmin

        userEntity.setContactnumber(signupUserRequest.getContactNumber());

        final UserEntity createdUserEntity = userBusinessService.signup(userEntity);

        SignupUserResponse signupUserResponse =
                new SignupUserResponse().id(createdUserEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupUserResponse>(signupUserResponse, HttpStatus.CREATED);
    }

  /**
   * Handles "/user/signin" endpoint which is used for user authentication. The user authenticates in the
   * application and after successful authentication, JWT token is given to a user.
   *
   * @param RequestHeader("authorization") - Base64 format of username:password
   * @return ResponseEntity<SigninResponse>
   * @throws AuthenticationFailedException
   */
  @RequestMapping(
      method = RequestMethod.POST,
      path = "/signin",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<SigninResponse> signin(
      @RequestHeader("authorization") final String authorization)
      throws AuthenticationFailedException {
        try {
            //Split the authorization string which will be in the format "Basic <Base64 String>"
            //e.g., Basic dXNlcm5hbWU6cGFzc3dvcmQ=
            //Decoded Base64 string will be in the format <username>:<password>
            byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
            String decodedText = new String(decode);
            String[] decodedArray = decodedText.split(":");

            //invoke signin method on UserBusinessService object by passing username and password inputs
            final UserAuthEntity userAuthEntity = userBusinessService.signin(decodedArray[0], decodedArray[1]);
            UserEntity userEntity = userAuthEntity.getUser();

            //If any authentication error occurs exception would have been thrown by this time
            //Otherwise it is a successful signin, so build SigninResponse object

            SigninResponse signinResponse = new SigninResponse()
                    .id(UUID.fromString(userEntity.getUuid()).toString())
                    .message("Authenticated successfully");

            //add the access-token information to the header
            HttpHeaders headers = new HttpHeaders();
            headers.add("access-token", userAuthEntity.getAccessToken());

            return new ResponseEntity<SigninResponse>(signinResponse, headers, HttpStatus.OK);
        }
        catch(ArrayIndexOutOfBoundsException aioe) {
            String exceptMsg = aioe.getMessage();

            throw new AuthenticationFailedException("ATH-102",
                    "Authentication failed - Invalid Credential input");
        }
        catch(IllegalArgumentException iae) {
            String exceptMsg = iae.getMessage();

            throw new AuthenticationFailedException("ATH-102",
                    "Authentication failed - Invalid Credential input");
        }
    }

  /**
   * Handles "/user/signout" endpoint which is used to sign out from the Quora Application. The user
   * cannot access any other endpoint once he is signed out of the application.
   *
   * @param RequestHeader("authorization") - auth token
   * @return ResponseEntity<SignoutResponse>
   * @throws SignOutRestrictedException
   */
  @RequestMapping(
      method = RequestMethod.POST,
      path = "/signout",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<SignoutResponse> signout(
      @RequestHeader("authorization") final String authorization)
      throws SignOutRestrictedException {
        UserAuthEntity userAuthEntity = userBusinessService.signout(authorization);

        SignoutResponse signoutResponse = new SignoutResponse().id(userAuthEntity.getUuid()).message("SIGNED OUT SUCCESSFULLY");

        return new ResponseEntity<SignoutResponse>(signoutResponse, HttpStatus.OK);
    }

}
