package com.sanepidcenter.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeAndAuthControllerTest {

    @Test
    void home_ShouldReturnIndexView() {
        HomeController controller = new HomeController();
        assertEquals("index", controller.home());
    }

    @Test
    void authPages_ShouldReturnTemplateNames() {
        AuthPageController controller = new AuthPageController();
        assertEquals("login", controller.loginPage());
        assertEquals("register", controller.registerPage());
    }
}
