package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBusinessService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBusinessService userBusinessService;

    /**
     * Business login to handle the user delete request. Only Admin user can delete another user in the application
     * @param String uuid
     * @param String authorizationToken
     * @return UserEntity
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity deleteUser(final String uuid, final String authorizationToken) throws AuthorizationFailedException, UserNotFoundException {

        UserEntity userEntity = userBusinessService.getUserFromToken(authorizationToken);
        if(!userEntity.getRole().equals("admin")) {
            throw new AuthorizationFailedException("ATHR-003", "Unauthorized Access, Entered user is not an admin");
        }

        UserEntity deleteUserEntity = userDao.getUserByUuid(uuid);
        if(deleteUserEntity == null){
            throw new UserNotFoundException("USR-001", "User with entered uuid to be deleted does not exist");
        } else {
            userDao.deleteUser(deleteUserEntity);
        }

        return deleteUserEntity;
    }
}
