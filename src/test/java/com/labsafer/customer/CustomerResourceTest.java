package com.labsafer.customer;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class CustomerResourceTest {

    @Test
    void createListGetUpdateDelete() {
        // Create - usando email único com timestamp
        long timestamp = System.currentTimeMillis();
        String uniqueEmail = "test.user." + timestamp + "@example.com";
        
        String body = """
            {
              "lastName": "TestSilva",
              "middleName": "de",
              "firstName": "TestJoao",
              "email": "%s",
              "mobile": "+55 11 99999-0000"
            }
        """.formatted(uniqueEmail);

        String id = 
        given().contentType(ContentType.JSON).body(body)
        .when().post("/api/v1/customers")
        .then().statusCode(201)
               .body("email", equalTo(uniqueEmail))
               .extract().path("id");

        // List
        given().when().get("/api/v1/customers")
        .then().statusCode(200)
               .body("size()", greaterThanOrEqualTo(1));

        // Get
        given().when().get("/api/v1/customers/{id}", UUID.fromString(id))
        .then().statusCode(200)
               .body("firstName", equalTo("TestJoao"));

        // Update - enviando todos os campos obrigatórios
        String updateBody = """
            {
              "lastName": "TestSilva",
              "middleName": "de",
              "firstName": "TestJoao",
              "email": "%s",
              "mobile": "+55 11 98888-7777"
            }
        """.formatted(uniqueEmail);
        
        given().contentType(ContentType.JSON).body(updateBody)
        .when().put("/api/v1/customers/{id}", UUID.fromString(id))
        .then().statusCode(200)
               .body("mobile", containsString("7777"));

        // Delete
        given().when().delete("/api/v1/customers/{id}", UUID.fromString(id))
        .then().statusCode(204);
    }
}
