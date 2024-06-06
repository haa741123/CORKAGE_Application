package com.example.test;

import java.util.List;

public class VisionAPIRequest {
    public List<Request> requests;

    public static class Request {
        public Image image;
        public List<Feature> features;

        public static class Image {
            public String content;
        }

        public static class Feature {
            public String type;
            public int maxResults;
        }
    }
}
