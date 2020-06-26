package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnswerBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBusinessService userBusinessService;

    @Autowired
    private AnswerDao answerDao;

    /**
     * Handle the request to Create an answer
     * @param answerEntity
     * @param authorizationToken
     * @return AnswerEntity
     * @throws AuthorizationFailedException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(final AnswerEntity answerEntity, final String authorizationToken)
            throws AuthorizationFailedException {

        UserEntity userEntity = userBusinessService.getUserFromToken(authorizationToken);
        answerEntity.setUser(userEntity);
        answerDao.createAnswer(answerEntity);
        return answerEntity;
    }

    /**
     * Handle the request to retrieve the Answer from its uuid
     * @param answerUuid
     * @param authorizationToken
     * @return
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    public AnswerEntity getAnswerByAnswerUuid(final String answerUuid, final String authorizationToken)
            throws AuthorizationFailedException,AnswerNotFoundException {

        userBusinessService.getUserFromToken(authorizationToken);
        AnswerEntity answerEntity = answerDao.getAnswerByUuid(answerUuid);
        if(answerEntity == null){
            throw new AnswerNotFoundException("ANS-001", "Entered Answer uuid does not exist");
        }
        return answerEntity;
    }

    /**
     * Hande the request to update an Answer
     * @param answerEntity
     * @param authorizationToken
     * @return AnswerEntity
     * @throws AuthorizationFailedException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity updateAnswer(final AnswerEntity answerEntity, final String authorizationToken)
            throws AuthorizationFailedException {

        UserEntity userEntity = userBusinessService.getUserFromToken(authorizationToken);

        if(answerEntity.getUser().getId() == userEntity.getId()) {
            answerDao.updateAnswer(answerEntity);
        } else {
            throw new AuthorizationFailedException("ATHR-003", "Only the Answer owner can edit the Answer");
        }

        return answerEntity;
    }

    /**
     * Handle the request to delete an Answer
     * @param answerUuid
     * @param authorizationToken
     * @return AnswerEntity
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(final String answerUuid, final String authorizationToken) throws AuthorizationFailedException,AnswerNotFoundException {

        UserEntity userEntity = userBusinessService.getUserFromToken(authorizationToken);
        AnswerEntity answerEntity = answerDao.getAnswerByUuid(answerUuid);

        if(answerEntity == null){
            throw new AnswerNotFoundException("ANS-001", "Entered Answer uuid does not exist");
        }

        if(userEntity.getUuid().equals(answerEntity.getUser().getUuid()) || userEntity.getRole().equals("admin")) {
            answerDao.deleteAnswer(answerEntity);
        } else {
            throw new AuthorizationFailedException("ATHR-003", "Only the Answer owner or Admin can Delete this Answer");
        }

        return answerEntity;
    }

    /**
     * Handle the request to retrieve all the Answers given to a given  question
     * @param id
     * @param authorizationToken
     * @return List<AnswerEntity>
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    public List<AnswerEntity> getAllAnswersByQuestionId (final Integer id,final String authorizationToken) throws AuthorizationFailedException,InvalidQuestionException{
        userBusinessService.getUserFromToken(authorizationToken);

        List<AnswerEntity> answerEntities = answerDao.getAnswersByQuestionId(id);
        if(answerEntities == null || answerEntities.size() == 0){
            throw new InvalidQuestionException("ANS-002", "No Answer with specified Question Id exist.");
        }
        return answerEntities;
    }
}
