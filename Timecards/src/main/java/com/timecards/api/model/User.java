package com.timecards.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.HashMap;

/**
 * Created by javier on 9/16/13.
 *
 {"user":{"id":null,"tenant_id":null,"email":"y@n.co","authentication_token":null,"created_at":null,"updated_at":null,"first_name":"y","last_name":"y",
 "company_name":"y","customer_id":null,"last_4_digits":null,"wage":"0.0","deleted_at":null},
 "errors":{"password":["is too short (minimum is 8 characters)"]}}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @JsonProperty(value = "last_name")
    String lastName;
    @JsonProperty(value = "first_name")
    String firstName;
    @JsonProperty
    String id;
    @JsonProperty
    String token;
    @JsonProperty
    String email;
    @JsonProperty
    String password;
    @JsonProperty(value = "company_name")
    String company;
    @JsonProperty
    HashMap<String, String[]> errors;

    public HashMap<String, String[]> getErrors() {
        return errors;
    }

    public void setErrors(HashMap<String, String[]> errors) {
        this.errors = errors;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("User [firstName=");
        builder.append(firstName);
        builder.append(", lastName=");
        builder.append(lastName);
        builder.append(", id=");
        builder.append(id);
        builder.append(", token=");
        builder.append(token);
        builder.append(", email=");
        builder.append(email);
        builder.append("]");
        return builder.toString();
    }
}
