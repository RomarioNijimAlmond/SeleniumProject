package apitests;

import api.ApiFunctions;
import com.squareup.okhttp.OkHttpClient;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.hc.core5.http.HttpStatus;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.json.JSONArray;
import utils.Utils;

import java.util.List;

import static io.restassured.RestAssured.given;

public class GoRestCrudTests extends BaseTestApi {
    ApiFunctions apiFunctions;
    OkHttpClient client;
    private String baseUrl = "https://gorest.co.in/public/v2";

    @BeforeMethod
    public void initilizeObject() {
        apiFunctions = new ApiFunctions();
        client = new OkHttpClient();
    }

    /**
     * takes the difference between the male and female - if one of the gender count is more than the other than post relevant gender by the difference number to get even
     */
    @Test(description = "retrieve all genders and count them and make male and female count even")
    public void tc01_getUsersAllGendersAndCreateUsersWithSpecificGender() throws InterruptedException {
        String femaleData = "{\"id\":5114525,\"name\":\"" + faker.name() + "\",\"email\":\"" + faker.internet().emailAddress() + "\",\"gender\":\"female\",\"status\":\"active\"}";
        String maleData = "{\"id\":5114525,\"name\":\"" + faker.name() + "\",\"email\":\"" + faker.internet().emailAddress() + "\",\"gender\":\"male\",\"status\":\"active\"}";
        Response response = apiFunctions.GET(baseUrl + "/users", ContentType.JSON);
        String responseJson = (response.getBody()).asString();
        JsonPath jsonPath = new JsonPath(responseJson);
        List<String> genders = jsonPath.getList("gender");
        long maleCount = genders.stream().filter(g -> g.equalsIgnoreCase("male")).count();
        long femaleCount = genders.stream().filter(g -> g.equalsIgnoreCase("female")).count();
        List<String> ids = jsonPath.getList("id");
        long difference = maleCount + (-femaleCount);
        if (maleCount > femaleCount) {
            for (int i = 0; i <= difference; i++) ;
            apiFunctions.POST(baseUrl + "/users/" + ids, femaleData, ContentType.JSON, Utils.readProperty("apiToken"));
        } else {
            apiFunctions.POST(baseUrl + "/users/" + ids, maleData, ContentType.JSON, Utils.readProperty("apiToken"));
            System.out.println(difference);
            System.out.println(genders);
            Assert.assertEquals(response.statusCode(), HttpStatus.SC_OK);
            Assert.assertEquals(femaleCount, maleCount);
        }
    }

    @Test(description = "delete all users that have inactive status")
    public void tc03_deleteInactiveUsers() throws InterruptedException {
        Response response = given()
                .header("Authorization", "Bearer " + Utils.readProperty("apiToken"))
                .log()
                .all()
                .when()
                .get(baseUrl + "/users")
                .then().log().all()
                .contentType(ContentType.JSON)
                .extract()
                .response();
        JSONArray users = new JSONArray(response.asString());

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if ("inactive".equals(user.getString("status"))) {
                int userId = user.getInt("id");
                given()
                        .when()
                        .log().all()
                        .delete(baseUrl + "/users/" + userId)
                        .then()
                        .log().all()
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK);
            }
        }
    }

    @Test(description = "modify all of the users email and modify their email extension to '.co.il' then assert all extensions changed to .co.il")
    public void tc04_updateUserEmailExtension() throws InterruptedException {
        Response response = given()
                .log()
                .all()
                .when()
                .get(baseUrl + "/users")
                .then().log().all()
                .contentType(ContentType.JSON)
                .extract()
                .response();
        JSONArray users = new JSONArray(response.asString());
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            String originalMail = user.getString("email");
            String updatedEmail = originalMail.replaceFirst("\\.(.*?)\\.", ".co.il");
            int userId = user.getInt("id");
            JSONObject updateData = new JSONObject();
            updateData.put("email", updatedEmail);
            given()
                    .header("Authorization", "Bearer " + Utils.readProperty("apiToken"))
                    .log()
                    .all()
                    .contentType(ContentType.JSON)
                    .when()
                    .body(updateData)
                    .put(baseUrl + "/users/" + userId)
                    .then()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK);

//---------------------------------------------------------
            apiFunctions.GET(baseUrl + "/users" + userId, ContentType.JSON);
            String updatedEmailFromServer = response.jsonPath().getString("email");
            Assert.assertTrue(updatedEmailFromServer.endsWith(".co.il"), "Email extension is not '.co.il'");
        }
    }
}


