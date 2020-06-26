package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.AnswerResponse;
import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.AnswerEditRequest;
import com.upgrad.quora.api.model.AnswerEditResponse;
import com.upgrad.quora.api.model.AnswerDeleteResponse;
import com.upgrad.quora.api.model.AnswerDetailsResponse;

import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;

import com.upgrad.quora.service.exception.AuthorizationFailedException;

import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import com.upgrad.quora.service.business.AnswerBusinessService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class - AnswerController
 * This Class will handle all  request for Answer endpoints.
 */
@RestController
@RequestMapping("/")
public class AnswerController {

    @Autowired
    private AnswerBusinessService answerBusinessService;

    @Autowired
    private QuestionBusinessService questionBusinessService;

    /**
     * Handles "question/{questionId}/answer/create" endpoint which is used to create answer for a
     * given question Uuid.
     *
     * @param @RequestHeader("authorization") - JWT Token can be found from "/user/signin" endpoints
     * @param @PathVariable("questionId")
     * @param @AnswerRequest
     *
     * @return ResponseEntity<AnswerResponse>
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     *
     */
    @RequestMapping(method = RequestMethod.POST,
            path="question/{questionId}/answer/create",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("questionId") final String questionUuid,
            final AnswerRequest request)
            throws AuthorizationFailedException, InvalidQuestionException {

        //Retrieve the question entity from question uuid
        QuestionEntity quesEntity = questionBusinessService.getQuestion(questionUuid);

        //Prepare the answer entity object which will used in create answer
        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setQuestion(quesEntity);
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setAns(request.getAnswer());
        Timestamp date = new Timestamp(System.currentTimeMillis());
        answerEntity.setDate(date);

        //Invoke the createAnswer method from answerBusinessService object with answerEntity and authorization as parameter
        final AnswerEntity createdAnswerEntity = answerBusinessService.createAnswer(answerEntity,authorization);

        //If the Answer got created successfully it will return AnswerResponse else it will throw exception
        final AnswerResponse answerResponse = new AnswerResponse().id(createdAnswerEntity.getUuid()).status("ANSWER CREATED");

        return new ResponseEntity<AnswerResponse>(answerResponse,HttpStatus.CREATED);

    }

    /**
     * Handles "answer/edit/{answerId}" endpoint which is used to edit an existing Answer.
     *
     * @param @PathVariable("answerId")
     * @param @RequestHeader("authorization") - JWT Token can be found from "/user/signin" endpoints
     * @param @AnswerEditRequest
     *
     * @return ResponseEntity<AnswerEditResponse>
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    @RequestMapping(method = RequestMethod.PUT,
            path="answer/edit/{answerId}",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> editAnswerContent(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("answerId") final String answerUuid,
            AnswerEditRequest editRequest)
            throws AuthorizationFailedException,AnswerNotFoundException{
        // 1. First Get the Answer.
        // 2. Then update the Answer Content
        // Finally update Answer in DB.
        //Retrieve the answer entity from answer uuid from answerBusinessService
        AnswerEntity answerEntity = answerBusinessService.getAnswerByAnswerUuid(answerUuid,authorization);
        answerEntity.setAns(editRequest.getContent());
        Timestamp date = new Timestamp(System.currentTimeMillis());
        answerEntity.setDate(date);

        //Update the answer entity in the DB by invoking updateAnswer from answerBusinessService
        AnswerEntity updatedAnswerEntity = answerBusinessService.updateAnswer(answerEntity,authorization);

        //If the Answer got updated successfully it will return AnswerEditResponse else it will throw exception
        AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(updatedAnswerEntity.getUuid()).status("ANSWER EDITED");

        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }

    /**
     * Handles "answer/delete/{answerId}" endpoint which is used to delete an existing Answer.
     *
     * @param @PathVariable("answerId")
     * @param @RequestHeader("authorization") - JWT Token can be found from "/user/signin" endpoints
     *
     * @return ResponseEntity<AnswerDeleteResponse>
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    @RequestMapping(method = RequestMethod.DELETE,
            path="answer/delete/{answerId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswer(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("answerId") final String answerUuid)
            throws AuthorizationFailedException, AnswerNotFoundException {
        // Who all are allowed to delete the answer only user or admin as well.
        //Delete the answer entity from the DB by invoking deleteAnswer from answerBusinessService
        AnswerEntity updatedAnswerEntity = answerBusinessService.deleteAnswer(answerUuid, authorization);

        //If the Answer got deleted successfully it will return AnswerDeleteResponse else it will throw exception
        AnswerDeleteResponse deleteResponse = new AnswerDeleteResponse().id(updatedAnswerEntity.getUuid()).status("ANSWER DELETED");

        return new ResponseEntity<AnswerDeleteResponse>(deleteResponse, HttpStatus.OK);

    }

    /**
     * Handles "answer/all/{questionId}" endpoint which is used to retrieve all answer for a given question uuid.
     *
     * @param @PathVariable("questionId")
     * @param @RequestHeader("authorization") - JWT Token can be found from "/user/signin" endpoints
     *
     * @return ResponseEntity<AnswerDetailsResponse>
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @RequestMapping(method = RequestMethod.GET,
            path="answer/all/{questionId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
        public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswerToQuestion(
                @RequestHeader("authorization") final String authorization,
                @PathVariable("questionId") final String questionUuid)
            throws  AuthorizationFailedException,InvalidQuestionException {
        // First get question using questionUuid, then find the questionId and for that question Id get all the answers.
        QuestionEntity quesEntity = questionBusinessService.getQuestion(questionUuid);

        //Get all question by question ID by invoking getAllAnswerByQuestionId method from answerBusinessService
        List<AnswerEntity> answerEntities = answerBusinessService.getAllAnswersByQuestionId(quesEntity.getId(),authorization);

        // Build the answer responses.
        List<AnswerDetailsResponse> answerDetailsResponses = new ArrayList<>();

        for (AnswerEntity answerEntity : answerEntities) {
            answerDetailsResponses.add(
                    new AnswerDetailsResponse()
                            .id(answerEntity.getUuid())
                            .questionContent(answerEntity.getQuestion().getContent())
                             .answerContent(answerEntity.getAns())
            ) ;
        }
        return new ResponseEntity<>(answerDetailsResponses,HttpStatus.OK);

    }

}
