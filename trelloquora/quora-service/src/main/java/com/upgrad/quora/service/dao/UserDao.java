package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    public UserEntity createUser(UserEntity userEntity) {
        entityManager.persist(userEntity);
        return userEntity;
    }
  
    public UserEntity getUserByUsername(final String username) {

        try {
            return entityManager.createNamedQuery(
                    "userByUsername", UserEntity.class)
                    .setParameter("username", username)
                    .getSingleResult();
        }
        catch(NoResultException nre) {
            return null;
        }
    }

    /*
      getUserByUuid - Get UserEntity Object from its UUID
   */
    public UserEntity getUserByUuid(final String uuid) {
        try{
            return entityManager.createNamedQuery("userByUuid", UserEntity.class).setParameter("uuid", uuid).getSingleResult();
        }
        catch(NoResultException e) {
            return null;
        }
    }

    public UserEntity getUserByEmail(final String email) {
        try {
            return entityManager.createNamedQuery(
                    "userByEmail", UserEntity.class)
                    .setParameter("email", email)
                    .getSingleResult();
        }
        catch(NoResultException nre) {
            return null;
        }
    }

    public UserAuthEntity createAuthToken(final UserAuthEntity authTokenEntity) {
        entityManager.persist(authTokenEntity);
        return authTokenEntity;
    }

    public UserEntity deleteUser(UserEntity userEntity) {
        entityManager.remove(userEntity);
        return userEntity;
    }

    /*
        getUserAuthToken - This Method will return the AuthToken for the Signed In User
     */
    public UserAuthEntity getUserAuthToken(final String accessToken) {
        try {
            return entityManager.createNamedQuery("userAuthByAccessToken", UserAuthEntity.class).setParameter("accessToken", accessToken).getSingleResult();
        } 
        catch (NoResultException nre) {
            return null;
        }
    }

    public void updateUserAuth(final UserAuthEntity updatedUserAuthEntity) {
        entityManager.merge(updatedUserAuthEntity);
    }

    public void updateUser(final UserEntity updateUserEntity) {
        entityManager.merge(updateUserEntity);
    }	

    public UserAuthEntity getUserAuth(final String accessToken) {
        try {
            return entityManager.createNamedQuery("userAuthByAccessToken", UserAuthEntity.class).setParameter("accessToken", accessToken).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}