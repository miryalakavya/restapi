package com.upgrad.quora.api.controller;

import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.upgrad.quora.api.model.QuestionRequest;
import com.upgrad.quora.api.model.QuestionResponse;
import com.upgrad.quora.api.model.QuestionDetailsResponse;
import com.upgrad.quora.api.model.QuestionDeleteResponse;
import com.upgrad.quora.api.model.QuestionEditRequest;
import com.upgrad.quora.api.model.QuestionEditResponse;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class - QuestionController
 * This Class will handle all request for Question endpoints.
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionBusinessService questionBusinessService;

    /**
     * Handles "/question/create" endpoint which is used to create a question.
     *
     * @param @QuestionRequest
     * @param @RequestHeader("authorization") - JWT Token can be found from "/user/signin" endpoints
     *
     * @return ResponseEntity<QuestionResponse>
     * @throws AuthorizationFailedException
     *
     */
    @RequestMapping(method = RequestMethod.POST,
            path="/create",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(
            @RequestHeader("authorization") final String authorization,
            final QuestionRequest questionRequest)
            throws AuthorizationFailedException {

        //Preparing the question Entity
        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());
        Timestamp date = new Timestamp(System.currentTimeMillis());
        questionEntity.setDate(date);

        //Create the question in DB by invoking the createQuestion method in questionBusinessService
        final QuestionEntity createdQuestionEntity = questionBusinessService.createQuestion(questionEntity, authorization);

        //If the Question got created successfully it will return QuestionResponse else it will throw exception
        final QuestionResponse questionResponse = new QuestionResponse().id(createdQuestionEntity.getUuid()).status("QUESTION CREATED");

        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }

    /**
     * Handles "/question/all" endpoint which is used to retrieve all question.
     *
     * @param @RequestHeader("authorization") - JWT Token can be found from "/user/signin" endpoints
     *
     * @return <List<QuestionDetailsResponse>>
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     *
     */
    @RequestMapping(method = RequestMethod.GET,
            path="/all",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {
        //Retrieve all the question from DB by invoking the getAllQuestions method in questionBusinessService
        List<QuestionEntity> questionEntities = questionBusinessService.getAllQuestions(authorization);

        //If the Questions got retrieved successfully it will return QuestionDetailsResponse else it will throw exception
        List<QuestionDetailsResponse> questionResponses = entitiesToResponse(questionEntities);

        return new ResponseEntity(questionResponses, HttpStatus.OK);
    }


    /**
     * Handles "/question/edit/{questionId}" endpoint which is used to edit a given question.
     *
     * @param @QuestionRequest
     * @param @PathVariable("questionId")
     * @param @RequestHeader("authorization") - JWT Token can be found from "/user/signin" endpoints
     *
     * @return QuestionResponse
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     *
     */
    @RequestMapping(method = RequestMethod.PUT,
            path="/edit/{questionId}",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionContent (
            @RequestHeader("authorization") final String authorization,
            @PathVariable("questionId") final String questionId,
            final QuestionEditRequest questionRequest)
            throws AuthorizationFailedException, InvalidQuestionException {

        //Retrieve the question from DB by invoking the getQuestion method in questionBusinessService
        QuestionEntity questionEntity = questionBusinessService.getQuestion(questionId);
        questionEntity.setContent(questionRequest.getContent());
        Timestamp date = new Timestamp(System.currentTimeMillis());
        questionEntity.setDate(date);

        //Update the question to the  DB by invoking the updateQuestion method in questionBusinessService
        QuestionEntity updatedQuestionEntity = questionBusinessService.updateQuestion(questionEntity, authorization);

        //If the Questions got edited successfully it will return QuestionResponse else it will throw exception
        QuestionEditResponse questionResponse = new QuestionEditResponse().id(updatedQuestionEntity.getUuid()).status("QUESTION EDITED");

        return new ResponseEntity<QuestionEditResponse>(questionResponse, HttpStatus.OK);
    }

    /**
     * Handles "/question/delete/{questionId}" endpoint which is used to delete a given question.
     *
     * @param @PathVariable("questionId")
     * @param @RequestHeader("authorization") - JWT Token can be found from "/user/signin" endpoints
     *
     * @return QuestionResponse
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     *
     */
    @RequestMapping(method = RequestMethod.DELETE,
            path="/delete/{questionId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("questionId") final String questionId)
            throws AuthorizationFailedException, InvalidQuestionException {

        //Delete the question entity from the DB by invoking deleteQuestion from questionBusinessService
        QuestionEntity updatedQuestionEntity = questionBusinessService.deleteQuestion(questionId, authorization);

        //If the question got deleted successfully it will return QuestionResponse else it will throw exception
        QuestionDeleteResponse questionResponse = new QuestionDeleteResponse().id(updatedQuestionEntity.getUuid()).status("QUESTION DELETED");

        return new ResponseEntity<QuestionDeleteResponse>(questionResponse, HttpStatus.OK);
    }

    /**
     * Handles "/question/all/{userId}" endpoint which is used to retrieve all questions by an user.
     *
     * @param @PathVariable("userId")
     * @param @RequestHeader("authorization") - JWT Token can be found from "/user/signin" endpoints
     *
     * @return QuestionResponse
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     *
     */
    @RequestMapping(method = RequestMethod.GET,
            path="/all/{userId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("userId") final String userId)
            throws AuthorizationFailedException, InvalidQuestionException, UserNotFoundException {

        //Retrieve all the question raised by an user by invoking getAllQuestionsByUser method from questionBusinessService Object
        List<QuestionEntity> questionEntities = questionBusinessService.getAllQuestionsByUser(userId, authorization);

        //Retrieve the QuestionDetailsResponse list
        List<QuestionDetailsResponse> questionResponses = entitiesToResponse(questionEntities);

        return new ResponseEntity(questionResponses, HttpStatus.OK);
    }

    /**
     * Utility method to use adn convert the list of QuestionEntities to list of QuestionDetailsResponse
     *
     * @param questionEntities
     * @return List<QuestionDetailsResponse>
     */
    private List<QuestionDetailsResponse> entitiesToResponse(List<QuestionEntity> questionEntities) {
        List<QuestionDetailsResponse> questionResponses = new ArrayList<>();
        for (QuestionEntity questionEntity : questionEntities) {
            questionResponses.add(
                    new QuestionDetailsResponse()
                            .id(questionEntity.getUuid())
                            .content(questionEntity.getContent())
            ) ;
        }

        return questionResponses;
    }
}
