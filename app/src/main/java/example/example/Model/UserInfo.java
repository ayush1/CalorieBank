package example.example.Model;

/**
 * Created by ayushgarg on 19/06/17.
 */

public class UserInfo {

    String token;
    String weight;
    String email;
    String gender;
    String caloriesBurn;
    float height;
    float BMI;
    int age;

    static UserInfo userInfo;

    private UserInfo() {
    }

    public static UserInfo getInstance(){
        if(userInfo == null){
            userInfo = new UserInfo();
        }
        return userInfo;
    }

    public float getBMI() {
        return BMI;
    }

    public UserInfo setBMI(float BMI) {
        this.BMI = BMI;
        return this;
    }

    public String getToken() {
        return token;
    }

    public UserInfo setToken(String token) {
        this.token = token;
        return this;
    }

    public int getAge() {
        return age;
    }

    public UserInfo setAge(int age) {
        this.age = age;
        return this;
    }

    public float getHeight() {
        return height;
    }

    public UserInfo setHeight(float height) {
        this.height = height;
        return this;
    }

    public String getWeight() {
        return weight;
    }

    public UserInfo setWeight(String weight) {
        this.weight = weight;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserInfo setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public UserInfo setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public String getCaloriesBurn() {
        return caloriesBurn;
    }

    public UserInfo setCaloriesBurn(String caloriesBurn) {
        this.caloriesBurn = caloriesBurn;
        return this;
    }
}
