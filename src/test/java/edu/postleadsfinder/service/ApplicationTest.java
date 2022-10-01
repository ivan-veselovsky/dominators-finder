package edu.postleadsfinder.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest
class ApplicationTest {

    @Autowired
    private GraphPostLeadsFinderRestController controller;

    @Test
    public void contextLoads() {
        then(controller).isNotNull();
    }
}