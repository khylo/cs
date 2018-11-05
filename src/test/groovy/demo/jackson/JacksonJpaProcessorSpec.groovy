package com.example.demo.jackson

class JacksonJpaProcessorSpec {
    public void testProcess(){
        given:
        JacksonJpaProcessor p = new JacksonJpaProcessor();

        when:
        p.process();

        then:
        p.getStore().size() == expectedSize
        where:
        file        || expectedSize
        "test.json" || 2

    }
}
