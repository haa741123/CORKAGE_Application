package com.example.test;

import java.util.List;

public class VisionAPIResponse {
    public List<Response> responses;

    public static class Response {
        public List<EntityAnnotation> textAnnotations;

        public static class EntityAnnotation {
            public String locale;
            public String description;
            public BoundingPoly boundingPoly;

            public static class BoundingPoly {
                public List<Vertex> vertices;

                public static class Vertex {
                    public int x;
                    public int y;
                }
            }
        }
    }
}
