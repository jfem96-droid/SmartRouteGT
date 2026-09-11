package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.service.BfsDfsService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bfs-dfs")
public class BfsDfsController {

    private final BfsDfsService bfsDfsService;

    public BfsDfsController(BfsDfsService bfsDfsService) {
        this.bfsDfsService = bfsDfsService;
    }

    @GetMapping("/bfs")
    public List<Long> bfs(@RequestParam Long startStopId) {
        return bfsDfsService.bfs(startStopId);
    }

    @GetMapping("/dfs")
    public List<Long> dfs(@RequestParam Long startStopId) {
        return bfsDfsService.dfs(startStopId);
    }
}