package com.smartexpense;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartexpense.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testng.Assert.*;

/**
 * Comprehensive TestNG Test Suite for Smart Expense Splitter API
 * 
 * Test File: src/test/java/com/smartexpense/SmartExpenseSplitterApiTest.java
 * Test Class: SmartExpenseSplitterApiTest
 * Total Test Cases: 50
 * 
 * Coverage:
 * - User Management APIs (10 tests)
 * - Group Management APIs (12 tests)
 * - Expense Management APIs (15 tests)
 * - Settlement Management APIs (8 tests)
 * - Balance & Reporting APIs (5 tests)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Listeners(TestExecutionListener.class)
public class SmartExpenseSplitterApiTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Test data storage
    private Long userId1, userId2, userId3;
    private Long groupId1, groupId2;
    private Long expenseId1, expenseId2;
    private Long settlementId1;

    @BeforeClass
    public void setUp() {
        // Initialize test data
        userId1 = userId2 = userId3 = null;
        groupId1 = groupId2 = null;
        expenseId1 = expenseId2 = null;
        settlementId1 = null;
    }

    // ==================== USER MANAGEMENT API TESTS ====================

    @Test(priority = 1, description = "TC001: POST /users - Create User Success - API Method: addUser, Expected Status: 201 CREATED")
    public void testCreateUserSuccess() throws Exception {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPhoneNumber("+1234567890");

        MvcResult result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andReturn();

        User createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        userId1 = createdUser.getId();
        assertNotNull(userId1, "User ID should not be null");
    }

    @Test(priority = 2, description = "TC002: POST /users - Create User with Invalid Email - API Method: addUser, Expected Status: 400 BAD REQUEST")
    public void testCreateUserInvalidEmail() throws Exception {
        User user = new User();
        user.setName("Jane Smith");
        user.setEmail("invalid-email"); // Invalid email format
        user.setPhoneNumber("+9876543210");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test(priority = 3, description = "TC003: POST /users - Create User with Missing Name - API Method: addUser, Expected Status: 400 BAD REQUEST")
    public void testCreateUserMissingName() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        // Name is missing

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test(priority = 4, description = "TC004: GET /users - List All Users - API Method: listAllUsers, Expected Status: 200 OK")
    public void testListAllUsers() throws Exception {
        // Create another user first
        User user2 = new User();
        user2.setName("Alice Johnson");
        user2.setEmail("alice@example.com");
        user2.setPhoneNumber("+1111111111");

        MvcResult createResult = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser2 = objectMapper.readValue(createResult.getResponse().getContentAsString(), User.class);
        userId2 = createdUser2.getId();

        // List all users
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
    }

    @Test(priority = 5, description = "TC005: GET /users/{id} - Get User By ID Success - API Method: getUserById, Expected Status: 200 OK")
    public void testGetUserByIdSuccess() throws Exception {
        assertNotNull(userId1, "User ID should be set from previous test");

        mockMvc.perform(get("/users/" + userId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test(priority = 6, description = "TC006: GET /users/{id} - Get Non-Existent User - API Method: getUserById, Expected Status: 404 NOT FOUND")
    public void testGetUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/users/99999"))
                .andExpect(status().isNotFound());
    }

    @Test(priority = 7, description = "TC007: PUT /users/{id} - Update User Success - API Method: updateUser, Expected Status: 200 OK")
    public void testUpdateUserSuccess() throws Exception {
        assertNotNull(userId1, "User ID should be set");

        User updatedUser = new User();
        updatedUser.setName("John Updated Doe");
        updatedUser.setEmail("john.updated@example.com");
        updatedUser.setPhoneNumber("+9999999999");

        mockMvc.perform(put("/users/" + userId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated Doe"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));
    }

    @Test(priority = 8, description = "TC008: PUT /users/{id} - Update Non-Existent User - API Method: updateUser, Expected Status: 404 NOT FOUND")
    public void testUpdateUserNotFound() throws Exception {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");

        mockMvc.perform(put("/users/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test(priority = 9, description = "TC009: DELETE /users/{id} - Delete User Success - API Method: deleteUser, Expected Status: 204 NO CONTENT")
    public void testDeleteUserSuccess() throws Exception {
        // Create a user to delete
        User user = new User();
        user.setName("Temp User");
        user.setEmail("temp@example.com");

        MvcResult createResult = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(createResult.getResponse().getContentAsString(), User.class);
        Long tempUserId = createdUser.getId();

        mockMvc.perform(delete("/users/" + tempUserId))
                .andExpect(status().isNoContent());
    }

    @Test(priority = 10, description = "TC010: DELETE /users/{id} - Delete Non-Existent User - API Method: deleteUser, Expected Status: 404 NOT FOUND")
    public void testDeleteUserNotFound() throws Exception {
        mockMvc.perform(delete("/users/99999"))
                .andExpect(status().isNotFound());
    }

    // ==================== GROUP MANAGEMENT API TESTS ====================

    @Test(priority = 11, description = "TC011: POST /groups - Create Group Success - API Method: createGroup, Expected Status: 201 CREATED")
    public void testCreateGroupSuccess() throws Exception {
        assertNotNull(userId1, "User ID should be set");
        assertNotNull(userId2, "User ID should be set");

        Group group = new Group();
        group.setName("Trip to Paris");
        group.setDescription("Summer vacation expenses");
        List<Long> memberIds = new ArrayList<>();
        memberIds.add(userId1);
        memberIds.add(userId2);
        group.setMemberIds(memberIds);

        MvcResult result = mockMvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(group)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Trip to Paris"))
                .andReturn();

        Group createdGroup = objectMapper.readValue(result.getResponse().getContentAsString(), Group.class);
        groupId1 = createdGroup.getId();
        assertNotNull(groupId1, "Group ID should not be null");
    }

    @Test(priority = 12, description = "TC012: POST /groups - Create Group with Missing Name - API Method: createGroup, Expected Status: 400 BAD REQUEST")
    public void testCreateGroupMissingName() throws Exception {
        Group group = new Group();
        group.setDescription("Test description");
        // Name is missing

        mockMvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(group)))
                .andExpect(status().isBadRequest());
    }

    @Test(priority = 13, description = "TC013: GET /groups - List All Groups - API Method: listGroups, Expected Status: 200 OK")
    public void testListAllGroups() throws Exception {
        mockMvc.perform(get("/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test(priority = 14, description = "TC014: GET /groups/{id} - Get Group By ID Success - API Method: getGroupById, Expected Status: 200 OK")
    public void testGetGroupByIdSuccess() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");

        mockMvc.perform(get("/groups/" + groupId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(groupId1))
                .andExpect(jsonPath("$.name").value("Trip to Paris"));
    }

    @Test(priority = 15, description = "TC015: GET /groups/{id} - Get Non-Existent Group - API Method: getGroupById, Expected Status: 404 NOT FOUND")
    public void testGetGroupByIdNotFound() throws Exception {
        mockMvc.perform(get("/groups/99999"))
                .andExpect(status().isNotFound());
    }

    @Test(priority = 16, description = "TC016: POST /groups/{groupId}/members/{userId} - Add Member to Group Success - API Method: addMember, Expected Status: 200 OK")
    public void testAddMemberToGroupSuccess() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");
        // Create a third user
        User user3 = new User();
        user3.setName("Bob Wilson");
        user3.setEmail("bob@example.com");

        MvcResult createResult = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user3)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser3 = objectMapper.readValue(createResult.getResponse().getContentAsString(), User.class);
        userId3 = createdUser3.getId();

        mockMvc.perform(post("/groups/" + groupId1 + "/members/" + userId3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Member added successfully"));
    }

    @Test(priority = 17, description = "TC017: POST /groups/{groupId}/members/{userId} - Add Member to Non-Existent Group - API Method: addMember, Expected Status: 404 NOT FOUND")
    public void testAddMemberToGroupNotFound() throws Exception {
        assertNotNull(userId1, "User ID should be set");

        mockMvc.perform(post("/groups/99999/members/" + userId1))
                .andExpect(status().isNotFound());
    }

    @Test(priority = 18, description = "TC018: DELETE /groups/{groupId}/members/{userId} - Remove Member from Group Success - API Method: removeMember, Expected Status: 200 OK")
    public void testRemoveMemberFromGroupSuccess() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");
        assertNotNull(userId3, "User ID should be set");

        mockMvc.perform(delete("/groups/" + groupId1 + "/members/" + userId3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Member removed successfully"));
    }

    @Test(priority = 19, description = "TC019: DELETE /groups/{groupId}/members/{userId} - Remove Non-Existent Member - API Method: removeMember, Expected Status: 404 NOT FOUND")
    public void testRemoveMemberNotFound() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");

        mockMvc.perform(delete("/groups/" + groupId1 + "/members/99999"))
                .andExpect(status().isNotFound());
    }

    @Test(priority = 20, description = "TC020: DELETE /groups/{id} - Delete Group Success - API Method: deleteGroup, Expected Status: 204 NO CONTENT")
    public void testDeleteGroupSuccess() throws Exception {
        // Create a group to delete
        Group group = new Group();
        group.setName("Temp Group");
        group.setDescription("Temporary group for deletion");

        MvcResult createResult = mockMvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(group)))
                .andExpect(status().isCreated())
                .andReturn();

        Group createdGroup = objectMapper.readValue(createResult.getResponse().getContentAsString(), Group.class);
        Long tempGroupId = createdGroup.getId();

        mockMvc.perform(delete("/groups/" + tempGroupId))
                .andExpect(status().isNoContent());
    }

    @Test(priority = 21, description = "TC021: POST /groups - Create Second Group for Testing - API Method: createGroup, Expected Status: 201 CREATED")
    public void testCreateSecondGroup() throws Exception {
        assertNotNull(userId1, "User ID should be set");

        Group group = new Group();
        group.setName("Office Lunch");
        group.setDescription("Weekly office lunch expenses");
        List<Long> memberIds = new ArrayList<>();
        memberIds.add(userId1);
        group.setMemberIds(memberIds);

        MvcResult result = mockMvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(group)))
                .andExpect(status().isCreated())
                .andReturn();

        Group createdGroup = objectMapper.readValue(result.getResponse().getContentAsString(), Group.class);
        groupId2 = createdGroup.getId();
        assertNotNull(groupId2, "Group ID should not be null");
    }

    @Test(priority = 22, description = "TC022: GET /groups/{id} - Verify Group MemberIds - API Method: getGroupById, Expected Status: 200 OK")
    public void testVerifyGroupMemberIds() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");

        mockMvc.perform(get("/groups/" + groupId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberIds").isArray())
                .andExpect(jsonPath("$.memberIds.length()").value(greaterThanOrEqualTo(2)));
    }

    // ==================== EXPENSE MANAGEMENT API TESTS ====================

    @Test(priority = 23, description = "TC023: POST /expenses - Create Expense with EQUAL Split - API Method: addExpense, Expected Status: 201 CREATED")
    public void testCreateExpenseEqualSplit() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");
        assertNotNull(userId1, "User ID should be set");

        Expense expense = new Expense();
        expense.setGroupId(groupId1);
        expense.setDescription("Dinner at restaurant");
        expense.setAmount(150.00);
        expense.setPaidByUserId(userId1);
        expense.setDate(LocalDate.now());
        expense.setSplitType("EQUAL");

        MvcResult result = mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Dinner at restaurant"))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.splitType").value("EQUAL"))
                .andReturn();

        Expense createdExpense = objectMapper.readValue(result.getResponse().getContentAsString(), Expense.class);
        expenseId1 = createdExpense.getId();
        assertNotNull(expenseId1, "Expense ID should not be null");
    }

    @Test(priority = 24, description = "TC024: POST /expenses - Create Expense with EXACT Split - API Method: addExpense, Expected Status: 201 CREATED")
    public void testCreateExpenseExactSplit() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");
        assertNotNull(userId1, "User ID should be set");
        assertNotNull(userId2, "User ID should be set");

        Expense expense = new Expense();
        expense.setGroupId(groupId1);
        expense.setDescription("Movie tickets");
        expense.setAmount(100.00);
        expense.setPaidByUserId(userId1);
        expense.setDate(LocalDate.now());
        expense.setSplitType("EXACT");
        
        List<ExpenseShare> shares = new ArrayList<>();
        shares.add(new ExpenseShare(userId1, 60.00));
        shares.add(new ExpenseShare(userId2, 40.00));
        expense.setShares(shares);

        MvcResult result = mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.splitType").value("EXACT"))
                .andReturn();

        Expense createdExpense = objectMapper.readValue(result.getResponse().getContentAsString(), Expense.class);
        expenseId2 = createdExpense.getId();
    }

    @Test(priority = 25, description = "TC025: POST /expenses - Create Expense with PERCENT Split - API Method: addExpense, Expected Status: 201 CREATED")
    public void testCreateExpensePercentSplit() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");
        assertNotNull(userId1, "User ID should be set");
        assertNotNull(userId2, "User ID should be set");

        Expense expense = new Expense();
        expense.setGroupId(groupId1);
        expense.setDescription("Grocery shopping");
        expense.setAmount(200.00);
        expense.setPaidByUserId(userId1);
        expense.setDate(LocalDate.now());
        expense.setSplitType("PERCENT");
        
        List<ExpenseShare> shares = new ArrayList<>();
        shares.add(new ExpenseShare(userId1, 70.0)); // 70%
        shares.add(new ExpenseShare(userId2, 30.0)); // 30%
        expense.setShares(shares);

        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.splitType").value("PERCENT"));
    }

    @Test(priority = 26, description = "TC026: POST /expenses - Create Expense with Invalid Group ID - API Method: addExpense, Expected Status: 400 BAD REQUEST")
    public void testCreateExpenseInvalidGroup() throws Exception {
        assertNotNull(userId1, "User ID should be set");

        Expense expense = new Expense();
        expense.setGroupId(99999L); // Non-existent group
        expense.setDescription("Test expense");
        expense.setAmount(50.00);
        expense.setPaidByUserId(userId1);
        expense.setDate(LocalDate.now());
        expense.setSplitType("EQUAL");

        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest());
    }

    @Test(priority = 27, description = "TC027: POST /expenses - Create Expense with EXACT Split Invalid Amount - API Method: addExpense, Expected Status: 400 BAD REQUEST")
    public void testCreateExpenseExactSplitInvalidAmount() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");
        assertNotNull(userId1, "User ID should be set");

        Expense expense = new Expense();
        expense.setGroupId(groupId1);
        expense.setDescription("Test expense");
        expense.setAmount(100.00);
        expense.setPaidByUserId(userId1);
        expense.setDate(LocalDate.now());
        expense.setSplitType("EXACT");
        
        List<ExpenseShare> shares = new ArrayList<>();
        shares.add(new ExpenseShare(userId1, 60.00));
        shares.add(new ExpenseShare(userId2, 30.00)); // Total is 90, not 100
        expense.setShares(shares);

        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest());
    }

    @Test(priority = 28, description = "TC028: GET /expenses/group/{groupId} - List Expenses by Group - API Method: listExpensesByGroup, Expected Status: 200 OK")
    public void testListExpensesByGroup() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");

        mockMvc.perform(get("/expenses/group/" + groupId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3)));
    }

    @Test(priority = 29, description = "TC029: GET /expenses/{id} - Get Expense By ID Success - API Method: getExpenseById, Expected Status: 200 OK")
    public void testGetExpenseByIdSuccess() throws Exception {
        assertNotNull(expenseId1, "Expense ID should be set");

        mockMvc.perform(get("/expenses/" + expenseId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId1))
                .andExpect(jsonPath("$.description").value("Dinner at restaurant"));
    }

    @Test(priority = 30, description = "TC030: GET /expenses/{id} - Get Non-Existent Expense - API Method: getExpenseById, Expected Status: 404 NOT FOUND")
    public void testGetExpenseByIdNotFound() throws Exception {
        mockMvc.perform(get("/expenses/99999"))
                .andExpect(status().isNotFound());
    }

    @Test(priority = 31, description = "TC031: PUT /expenses/{id} - Update Expense Success - API Method: updateExpense, Expected Status: 200 OK")
    public void testUpdateExpenseSuccess() throws Exception {
        assertNotNull(expenseId1, "Expense ID should be set");
        assertNotNull(groupId1, "Group ID should be set");
        assertNotNull(userId1, "User ID should be set");

        Expense updatedExpense = new Expense();
        updatedExpense.setGroupId(groupId1);
        updatedExpense.setDescription("Updated dinner description");
        updatedExpense.setAmount(175.00);
        updatedExpense.setPaidByUserId(userId1);
        updatedExpense.setDate(LocalDate.now());
        updatedExpense.setSplitType("EQUAL");

        mockMvc.perform(put("/expenses/" + expenseId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedExpense)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated dinner description"))
                .andExpect(jsonPath("$.amount").value(175.00));
    }

    @Test(priority = 32, description = "TC032: PUT /expenses/{id} - Update Non-Existent Expense - API Method: updateExpense, Expected Status: 404 NOT FOUND")
    public void testUpdateExpenseNotFound() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");
        assertNotNull(userId1, "User ID should be set");

        Expense expense = new Expense();
        expense.setGroupId(groupId1);
        expense.setDescription("Test");
        expense.setAmount(50.00);
        expense.setPaidByUserId(userId1);
        expense.setDate(LocalDate.now());
        expense.setSplitType("EQUAL");

        mockMvc.perform(put("/expenses/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isNotFound());
    }

    @Test(priority = 33, description = "TC033: DELETE /expenses/{id} - Delete Expense Success - API Method: deleteExpense, Expected Status: 204 NO CONTENT")
    public void testDeleteExpenseSuccess() throws Exception {
        assertNotNull(expenseId2, "Expense ID should be set");

        mockMvc.perform(delete("/expenses/" + expenseId2))
                .andExpect(status().isNoContent());
    }

    @Test(priority = 34, description = "TC034: DELETE /expenses/{id} - Delete Non-Existent Expense - API Method: deleteExpense, Expected Status: 404 NOT FOUND")
    public void testDeleteExpenseNotFound() throws Exception {
        mockMvc.perform(delete("/expenses/99999"))
                .andExpect(status().isNotFound());
    }

    @Test(priority = 35, description = "TC035: POST /expenses - Create Expense with Negative Amount - API Method: addExpense, Expected Status: 400 BAD REQUEST")
    public void testCreateExpenseNegativeAmount() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");
        assertNotNull(userId1, "User ID should be set");

        Expense expense = new Expense();
        expense.setGroupId(groupId1);
        expense.setDescription("Invalid expense");
        expense.setAmount(-50.00); // Negative amount
        expense.setPaidByUserId(userId1);
        expense.setDate(LocalDate.now());
        expense.setSplitType("EQUAL");

        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest());
    }

    @Test(priority = 36, description = "TC036: POST /expenses - Create Expense with Missing Required Fields - API Method: addExpense, Expected Status: 400 BAD REQUEST")
    public void testCreateExpenseMissingFields() throws Exception {
        Expense expense = new Expense();
        expense.setDescription("Test");
        // Missing required fields: groupId, amount, paidByUserId, date, splitType

        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest());
    }

    @Test(priority = 37, description = "TC037: GET /expenses/group/{groupId} - List Expenses for Non-Existent Group - API Method: listExpensesByGroup, Expected Status: 200 OK (Empty Array)")
    public void testListExpensesNonExistentGroup() throws Exception {
        mockMvc.perform(get("/expenses/group/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== SETTLEMENT MANAGEMENT API TESTS ====================

    @Test(priority = 38, description = "TC038: POST /settlements - Create Settlement Success - API Method: addSettlement, Expected Status: 201 CREATED")
    public void testCreateSettlementSuccess() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");
        assertNotNull(userId1, "User ID should be set");
        assertNotNull(userId2, "User ID should be set");

        Settlement settlement = new Settlement();
        settlement.setGroupId(groupId1);
        settlement.setFromUserId(userId2);
        settlement.setToUserId(userId1);
        settlement.setAmount(25.00);
        settlement.setDate(LocalDate.now());
        settlement.setNote("Settled via cash");

        MvcResult result = mockMvc.perform(post("/settlements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settlement)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(25.00))
                .andReturn();

        Settlement createdSettlement = objectMapper.readValue(result.getResponse().getContentAsString(), Settlement.class);
        settlementId1 = createdSettlement.getId();
        assertNotNull(settlementId1, "Settlement ID should not be null");
    }

    @Test(priority = 39, description = "TC039: POST /settlements - Create Settlement with Same From and To User - API Method: addSettlement, Expected Status: 400 BAD REQUEST")
    public void testCreateSettlementSameUser() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");
        assertNotNull(userId1, "User ID should be set");

        Settlement settlement = new Settlement();
        settlement.setGroupId(groupId1);
        settlement.setFromUserId(userId1);
        settlement.setToUserId(userId1); // Same user
        settlement.setAmount(50.00);
        settlement.setDate(LocalDate.now());

        mockMvc.perform(post("/settlements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settlement)))
                .andExpect(status().isBadRequest());
    }

    @Test(priority = 40, description = "TC040: GET /settlements/group/{groupId} - List Settlements by Group - API Method: listSettlementsByGroup, Expected Status: 200 OK")
    public void testListSettlementsByGroup() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");

        mockMvc.perform(get("/settlements/group/" + groupId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test(priority = 41, description = "TC041: GET /settlements/{id} - Get Settlement By ID Success - API Method: getSettlementById, Expected Status: 200 OK")
    public void testGetSettlementByIdSuccess() throws Exception {
        assertNotNull(settlementId1, "Settlement ID should be set");

        mockMvc.perform(get("/settlements/" + settlementId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(settlementId1))
                .andExpect(jsonPath("$.amount").value(25.00));
    }

    @Test(priority = 42, description = "TC042: GET /settlements/{id} - Get Non-Existent Settlement - API Method: getSettlementById, Expected Status: 404 NOT FOUND")
    public void testGetSettlementByIdNotFound() throws Exception {
        mockMvc.perform(get("/settlements/99999"))
                .andExpect(status().isNotFound());
    }

    @Test(priority = 43, description = "TC043: DELETE /settlements/{id} - Delete Settlement Success - API Method: deleteSettlement, Expected Status: 204 NO CONTENT")
    public void testDeleteSettlementSuccess() throws Exception {
        // Create a settlement to delete
        assertNotNull(groupId1, "Group ID should be set");
        assertNotNull(userId1, "User ID should be set");
        assertNotNull(userId2, "User ID should be set");

        Settlement settlement = new Settlement();
        settlement.setGroupId(groupId1);
        settlement.setFromUserId(userId2);
        settlement.setToUserId(userId1);
        settlement.setAmount(10.00);
        settlement.setDate(LocalDate.now());

        MvcResult createResult = mockMvc.perform(post("/settlements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settlement)))
                .andExpect(status().isCreated())
                .andReturn();

        Settlement createdSettlement = objectMapper.readValue(createResult.getResponse().getContentAsString(), Settlement.class);
        Long tempSettlementId = createdSettlement.getId();

        mockMvc.perform(delete("/settlements/" + tempSettlementId))
                .andExpect(status().isNoContent());
    }

    @Test(priority = 44, description = "TC044: POST /settlements - Create Settlement with Invalid Group - API Method: addSettlement, Expected Status: 400 BAD REQUEST")
    public void testCreateSettlementInvalidGroup() throws Exception {
        assertNotNull(userId1, "User ID should be set");
        assertNotNull(userId2, "User ID should be set");

        Settlement settlement = new Settlement();
        settlement.setGroupId(99999L); // Non-existent group
        settlement.setFromUserId(userId1);
        settlement.setToUserId(userId2);
        settlement.setAmount(50.00);
        settlement.setDate(LocalDate.now());

        mockMvc.perform(post("/settlements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settlement)))
                .andExpect(status().isBadRequest());
    }

    @Test(priority = 45, description = "TC045: POST /settlements - Create Settlement with Negative Amount - API Method: addSettlement, Expected Status: 400 BAD REQUEST")
    public void testCreateSettlementNegativeAmount() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");
        assertNotNull(userId1, "User ID should be set");
        assertNotNull(userId2, "User ID should be set");

        Settlement settlement = new Settlement();
        settlement.setGroupId(groupId1);
        settlement.setFromUserId(userId1);
        settlement.setToUserId(userId2);
        settlement.setAmount(-10.00); // Negative amount
        settlement.setDate(LocalDate.now());

        mockMvc.perform(post("/settlements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settlement)))
                .andExpect(status().isBadRequest());
    }

    // ==================== BALANCE & REPORTING API TESTS ====================

    @Test(priority = 46, description = "TC046: GET /reports/groups/{groupId}/balances - Get Group Balances Success - API Method: getGroupBalances, Expected Status: 200 OK")
    public void testGetGroupBalancesSuccess() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");

        mockMvc.perform(get("/reports/groups/" + groupId1 + "/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].groupId").value(groupId1))
                .andExpect(jsonPath("$[0].userId").exists())
                .andExpect(jsonPath("$[0].userName").exists())
                .andExpect(jsonPath("$[0].netBalance").exists());
    }

    @Test(priority = 47, description = "TC047: GET /reports/groups/{groupId}/balances - Get Balances for Non-Existent Group - API Method: getGroupBalances, Expected Status: 404 NOT FOUND")
    public void testGetGroupBalancesNotFound() throws Exception {
        mockMvc.perform(get("/reports/groups/99999/balances"))
                .andExpect(status().isNotFound());
    }

    @Test(priority = 48, description = "TC048: GET /reports/groups/{groupId}/settlement-plan - Get Settlement Plan Success - API Method: getSettlementPlan, Expected Status: 200 OK")
    public void testGetSettlementPlanSuccess() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");

        mockMvc.perform(get("/reports/groups/" + groupId1 + "/settlement-plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId1))
                .andExpect(jsonPath("$.suggestions").isArray())
                .andExpect(jsonPath("$.transactionCount").exists());
    }

    @Test(priority = 49, description = "TC049: GET /reports/groups/{groupId}/settlement-plan - Get Settlement Plan for Non-Existent Group - API Method: getSettlementPlan, Expected Status: 404 NOT FOUND")
    public void testGetSettlementPlanNotFound() throws Exception {
        mockMvc.perform(get("/reports/groups/99999/settlement-plan"))
                .andExpect(status().isNotFound());
    }

    @Test(priority = 50, description = "TC050: GET /reports/groups/{groupId}/settlement-plan - Verify Settlement Plan Structure - API Method: getSettlementPlan, Expected Status: 200 OK")
    public void testVerifySettlementPlanStructure() throws Exception {
        assertNotNull(groupId1, "Group ID should be set");

        MvcResult result = mockMvc.perform(get("/reports/groups/" + groupId1 + "/settlement-plan"))
                .andExpect(status().isOk())
                .andReturn();

        SettlementPlan plan = objectMapper.readValue(result.getResponse().getContentAsString(), SettlementPlan.class);
        assertNotNull(plan, "Settlement plan should not be null");
        assertNotNull(plan.getSuggestions(), "Suggestions should not be null");
        assertNotNull(plan.getTransactionCount(), "Transaction count should not be null");
        
        if (!plan.getSuggestions().isEmpty()) {
            SettlementSuggestion suggestion = plan.getSuggestions().get(0);
            assertNotNull(suggestion.getFromUserId(), "From user ID should not be null");
            assertNotNull(suggestion.getToUserId(), "To user ID should not be null");
            assertNotNull(suggestion.getAmount(), "Amount should not be null");
            assertTrue(suggestion.getAmount() > 0, "Amount should be positive");
        }
    }
}

