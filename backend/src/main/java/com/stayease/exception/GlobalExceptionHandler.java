package com.stayease.exception;
import com.stayease.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.stayease.assistant.AiProviderException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class) public ResponseEntity<ApiError> handleApi(ApiException ex,HttpServletRequest req){return response(ex.getStatus(),ex.getMessage(),req,Map.of());}
    @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,HttpServletRequest req){Map<String,String> errors=new LinkedHashMap<>();for(FieldError e:ex.getBindingResult().getFieldErrors())errors.putIfAbsent(e.getField(),e.getDefaultMessage());return response(HttpStatus.BAD_REQUEST,"Request validation failed",req,errors);}
    @ExceptionHandler({ConstraintViolationException.class,HttpMessageNotReadableException.class,MissingServletRequestParameterException.class,MethodArgumentTypeMismatchException.class}) public ResponseEntity<ApiError> handleInvalid(Exception ex,HttpServletRequest req){return response(HttpStatus.BAD_REQUEST,"Request contains invalid or missing values",req,Map.of());}
    @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<ApiError> handleConflict(Exception ex,HttpServletRequest req){return response(HttpStatus.CONFLICT,"The request conflicts with existing data",req,Map.of());}
    @ExceptionHandler(PessimisticLockingFailureException.class) public ResponseEntity<ApiError> handleLockConflict(Exception ex,HttpServletRequest req){return response(HttpStatus.CONFLICT,"Room inventory is being updated; please retry the request",req,Map.of());}
    @ExceptionHandler(AiProviderException.class) public ResponseEntity<ApiError> handleAiUnavailable(Exception ex,HttpServletRequest req){return response(HttpStatus.SERVICE_UNAVAILABLE,"The AI assistant is temporarily unavailable. Please retry, or try a shorter hotel search.",req,Map.of());}
    @ExceptionHandler(Exception.class) public ResponseEntity<ApiError> handleOther(Exception ex,HttpServletRequest req){return response(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected server error occurred",req,Map.of());}
    private ResponseEntity<ApiError> response(HttpStatus status,String message,HttpServletRequest req,Map<String,String> fields){return ResponseEntity.status(status).body(new ApiError(LocalDateTime.now(),status.value(),status.getReasonPhrase(),message,req.getRequestURI(),fields));}
}
