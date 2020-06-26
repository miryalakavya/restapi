package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    /**
     * Business login to handle the user signin requests
     * @param UserEntity
     * @return UserEntity
     * @throws SignUpRestrictedException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(UserEntity userEntity) throws SignUpRestrictedException {
        UserEntity existingUsername = userDao.getUserByUsername(userEntity.getUsername());
        UserEntity existingEmailid = userDao.getUserByEmail(userEntity.getEmail());

        if(existingUsername != null) {
            throw new SignUpRestrictedException("SGR-001",
                    "Try any other Username, this Username has already been taken");
        }
        if(existingEmailid != null) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other" +
                    " emailId");
        }

        //encrypt the user password, split the encrypted code into salt and text and assign to the UserEntity object
        String[] encryptedText = passwordCryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        UserEntity persistedUserEntity = userDao.createUser(userEntity);

        return persistedUserEntity;

    }

    /**
     * Business logic to handle the user signin requests
     * @param String username
     * @param String password
     * @return UserAuthEntity
     * @throws AuthenticationFailedException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity signin(final String username, final String password) throws AuthenticationFailedException {
        UserEntity userEntity = userDao.getUserByUsername(username);
        if(userEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This username does not exist");
        }

        // encrypt the password provided by the user during login using the salt stored in the database for that user
        final String encryptedPassword = passwordCryptographyProvider.encrypt(password, userEntity.getSalt());
        //check if the encrypted form of user provided password is equal to encrypted password store in the database
        if(encryptedPassword.equals(userEntity.getPassword())) {
            // generate a JWT Auth Token for the user signin
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            UserAuthEntity userAuthEntity = new UserAuthEntity();
            userAuthEntity.setUser(userEntity);

            //set expiry time as one hour for the auth token
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(1);

            userAuthEntity.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
            userAuthEntity.setLoginAt(now);
            userAuthEntity.setExpiresAt(expiresAt);
            userAuthEntity.setLoginAt(now);
            userAuthEntity.setUuid(userEntity.getUuid());
            userDao.createAuthToken(userAuthEntity);
            return userAuthEntity;
        }
        else {
            throw new AuthenticationFailedException("ATH-002", "Password failed");
        }
    }

    /**
     * Business login to retrieve the Details of a Signed-in User
     * @param String userUuid
     * @param String accesstoken
     * @return
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    public UserEntity getUserProfile(final String userUuid, final String accesstoken) throws AuthorizationFailedException, UserNotFoundException {

        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accesstoken);
        if(userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        ZonedDateTime isUserLogout = userAuthEntity.getLogoutAt();
        if (isUserLogout == null) {
            UserEntity userEntity = userDao.getUserByUuid(userUuid);
            if (userEntity == null) {
                throw new UserNotFoundException("USR-001", "User with entered uuid does not exist");
            }
            return userEntity;
        }

        throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
    }

    /**
     * Business login to handle the user sign out requests
     * @param accesstoken
     * @return
     * @throws SignOutRestrictedException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity signout(final String accesstoken) throws SignOutRestrictedException {

        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accesstoken);

        // User is considered as actively signed-in only if Auth token is available in the database and
        // user logout time is not filled-in in the database

        if(userAuthEntity == null || userAuthEntity.getLogoutAt()!=null) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        }

        final ZonedDateTime now = ZonedDateTime.now();
        userAuthEntity.setLogoutAt(now);
        userDao.updateUserAuth(userAuthEntity);

        return userAuthEntity;
    }

    /**
     * Utility method to retrieve the user auth token from the database
     * @param authorizationToken
     * @return
     * @throws AuthorizationFailedException
     */
    public UserEntity getUserFromToken(String authorizationToken) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorizationToken);

        if(userAuthEntity == null){
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        else if(userAuthEntity.getLogoutAt() != null ){
            throw new AuthorizationFailedException("ATHR-002", "User is signed out");
        }

        UserEntity userEntity = userAuthEntity.getUser();

        return userEntity;
    }

}