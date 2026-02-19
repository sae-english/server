package com.englishmovies.server.work.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequiredArgsConstructor
public class WorkController {

}
