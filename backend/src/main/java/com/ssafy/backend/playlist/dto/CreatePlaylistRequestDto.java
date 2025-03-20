package com.ssafy.backend.playlist.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CreatePlaylistRequestDto {
    private String name;
    private String description;
    private MultipartFile image; // file
    private List<Integer> trackIds;
}
