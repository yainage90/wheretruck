package com.gamakdragons.wheretruck.domain.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.UUID;

import com.gamakdragons.wheretruck.TestIndexUtil;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.favorite.entity.Favorite;
import com.gamakdragons.wheretruck.domain.favorite.service.FavoriteService;
import com.gamakdragons.wheretruck.domain.favorite.service.FavoriteServiceImpl;
import com.gamakdragons.wheretruck.domain.user.dto.Role;
import com.gamakdragons.wheretruck.domain.user.entity.User;
import com.gamakdragons.wheretruck.test_config.ElasticSearchTestConfig;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {UserServiceImpl.class, ElasticSearchTestConfig.class, FavoriteServiceImpl.class, TestIndexUtil.class}, 
                properties = {"spring.config.location=classpath:application-test.yml"})
@Slf4j
public class UserServiceImplPlatformTest {

    @Autowired
    private UserService userService;
    
    @Autowired
    private FavoriteService favoriteService;

    @BeforeAll
    public static void beforeAll() {
        TestIndexUtil.createElasticSearchTestContainer();
    }

    @AfterAll
    public static void afterAll() {
        TestIndexUtil.closeElasticSearchTestContainer();
    }
    
    @BeforeEach
    public void beforeEach() throws IOException {
        TestIndexUtil.initRestHighLevelClient();
        TestIndexUtil.deleteTestUserIndex();
        TestIndexUtil.deleteTestFavoriteIndex();

        TestIndexUtil.createTestFavoriteIndex();
        TestIndexUtil.createTestUserIndex();

        createTestUserData();
    }

    @AfterEach
    public void afterEach() throws IOException {
        TestIndexUtil.deleteTestUserIndex();
        TestIndexUtil.deleteTestFavoriteIndex();
    }


    @Test
    void testGetById() {

        User user = createTestUserData();
        indexTestUserData(user);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        User resultUser = userService.getById(user.getId());
        assertThat(resultUser, equalTo(user));

    }

    @Test
    void testSaveUser() {

        User user = createTestUserData();

        IndexUpdateResultDto indexResult = userService.saveUser(user);
        log.info("user index result: " + indexResult.getResult() + ", user id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));
        assertThat(indexResult.getId(), is(user.getId()));

    }

    @Test
    void testChangeNickname() {

        User user = createTestUserData();
        indexTestUserData(user);

        String nickNameToUpdate = "updated " + user.getNickName();
        user.setNickName(nickNameToUpdate);

        UpdateResultDto updateResult = userService.changeNickName(user.getId(), nickNameToUpdate);
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(updateResult.getResult(), is("UPDATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(userService.getById(user.getId()).getNickName(), equalTo(nickNameToUpdate));
    }

    @Test
    void testChangeRole() {

        User user = createTestUserData();
        indexTestUserData(user);

        UpdateResultDto updateResult = userService.changeRole(user.getId(), Role.USER);
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(updateResult.getResult(), is("UPDATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(userService.getById(user.getId()).getRole(), is(Role.USER));
    }


    @Test
    void testDeleteUser() {

        User user = createTestUserData();
        indexTestUserData(user);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        DeleteResultDto deleteResult = userService.deleteUser(user.getId());

        assertThat(deleteResult.getResult(), is("DELETED"));
        try {
            Thread.sleep(200);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(userService.getById(user.getId()), nullValue());
    }

    @Test
    void testDeleteFavoritesWhenUserDeleted() {

        User user = createTestUserData();
        indexTestUserData(user);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Favorite favorite = new Favorite();
        favorite.setTruckId(UUID.randomUUID().toString());
        favorite.setUserId(user.getId());

        favoriteService.saveFavorite(favorite);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(favoriteService.findByUserId(user.getId()).getNumFound(), is(1));

        DeleteResultDto deleteResult = userService.deleteUser(user.getId());
        assertThat(deleteResult.getResult(), is("DELETED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(favoriteService.findByUserId(user.getId()).getNumFound(), is(0));

        assertThat(userService.getById(user.getId()), nullValue());
    }

    private void indexTestUserData(User user) {
        IndexUpdateResultDto indexResult = userService.saveUser(user);
        assertThat(indexResult.getId(), is(user.getId()));

        try {
            Thread.sleep(1500);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    private User createTestUserData() {

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setNickName("유저1");
        user.setRole(Role.OWNER);

        return user;
    }

}
