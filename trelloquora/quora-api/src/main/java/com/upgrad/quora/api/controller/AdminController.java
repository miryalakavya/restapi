package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.AdminBusinessService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminBusinessService adminBusinessService;

  /**
   * Handles /admin/user/{userId} endpoint which is used to delete a user from the Quora Application. Only
   * an admin is authorized to access this endpoint.
   *
   * @param PathVariable("userId")
   * @param RequestHeader("authorization")
   * @return
   * @throws AuthorizationFailedException
   * @throws UserNotFoundException
   */
  @RequestMapping(
      method = RequestMethod.DELETE,
      path = "/user/{userId}",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<UserDeleteResponse> deleteUser(
      @PathVariable("userId") final String userId,
      @RequestHeader("authorization") final String authorization)
      throws AuthorizationFailedException, UserNotFoundException {
        UserEntity deleteUserEntity = adminBusinessService.deleteUser(userId, authorization);
        UserDeleteResponse userDeleteResponse = new UserDeleteResponse().id(deleteUserEntity.getUuid()).status("USER SUCCESSFULLY DELETED");

        return new ResponseEntity<UserDeleteResponse>(userDeleteResponse, HttpStatus.OK);
    }
}
