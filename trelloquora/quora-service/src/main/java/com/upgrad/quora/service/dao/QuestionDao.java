package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class QuestionDao {

    @PersistenceContext
    private EntityManager entityManager;

    public QuestionEntity createQuestion(QuestionEntity questionEntity) {
        entityManager.persist(questionEntity);
        return questionEntity;
    }

    public QuestionEntity updateQuestion(QuestionEntity questionEntity) {
        entityManager.merge(questionEntity);
        return questionEntity;
    }

    public QuestionEntity deleteQuestion(QuestionEntity questionEntity) {
        entityManager.remove(questionEntity);
        return questionEntity;
    }

    public QuestionEntity getQuestion(final String uuid) {
        try {
            return entityManager.createNamedQuery("questionByUuid", QuestionEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<QuestionEntity> getQuestions() {
        try {
            return entityManager.createNamedQuery("questions", QuestionEntity.class).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<QuestionEntity> getQuestionsByUser(final String userUuid) {
        try {
            return entityManager.createNamedQuery("questionsByUser", QuestionEntity.class).setParameter("uuid", userUuid).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}

