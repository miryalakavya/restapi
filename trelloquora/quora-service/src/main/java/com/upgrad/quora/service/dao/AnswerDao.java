package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AnswerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public AnswerEntity createAnswer(AnswerEntity answerEntity) {
        entityManager.persist(answerEntity);
        return answerEntity;
    }

    public AnswerEntity getAnswerByUuid(final String Uuid) {
        try {
            return entityManager.createNamedQuery("answerByAnswerUuid", AnswerEntity.class).setParameter("uuid", Uuid).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<AnswerEntity> getAnswersByQuestionId(final Integer id) {
        try {
            return entityManager.createNamedQuery("answersByQuestionid", AnswerEntity.class).setParameter("id", id).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public AnswerEntity updateAnswer(AnswerEntity answerEntity) {
        entityManager.merge(answerEntity);
        return answerEntity;
    }

    public AnswerEntity deleteAnswer(AnswerEntity answerEntity) {
        entityManager.remove(answerEntity);
        return answerEntity;
    }
}
