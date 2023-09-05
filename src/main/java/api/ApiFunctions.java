package api;

import com.google.gson.JsonObject;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import utils.Utils;

import java.net.http.HttpClient;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiFunctions {

    public static Response GET(String url, ContentType contentType) {
        Response response = given()
                .log()
                .all()
                .contentType(contentType)
                .when()
                .get(url)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response();
        return response;
    }

    public static Response POST(String url, String body, ContentType contentType, String token) {
        Response response = given()
                .log()
                .all()
                .contentType(contentType)
                .body(body)
                .header("Authorization", "Bearer " + token)
                .when()
                .post(url)
                .then()
                .extract().response();
        return response;
    }

    public static Response PUT(String url, String body, ContentType contentType, String token) {
        Response response = given()
                .log()
                .all()
                .contentType(contentType)
                .header("Authorization", "Bearer " + token)
                .when()
                .put(body)
                .then()
                .extract().response();
        return response;
    }


    public static Response DELETE(String url, ContentType contentType, String token) {
        Response response = given()
                .log()
                .all()
                .contentType(contentType)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(url)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response();
        return response;
    }
}

