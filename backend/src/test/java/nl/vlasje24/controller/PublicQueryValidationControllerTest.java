package nl.vlasje24.controller;

import nl.vlasje24.config.SecurityConfig;
import nl.vlasje24.security.AppUserDetailsService;
import nl.vlasje24.service.ArtistService;
import nl.vlasje24.service.SearchService;
import nl.vlasje24.service.SongService;
import nl.vlasje24.service.TopArtistsService;
import nl.vlasje24.service.TopSongsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SongController.class, ArtistController.class, SearchController.class})
@Import(SecurityConfig.class)
class PublicQueryValidationControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean SongService songService;
    @MockitoBean TopSongsService topSongsService;
    @MockitoBean ArtistService artistService;
    @MockitoBean TopArtistsService topArtistsService;
    @MockitoBean SearchService searchService;
    @MockitoBean AppUserDetailsService appUserDetailsService;

    @Test
    void topSongs_rejectsUnknownSortAndOversizedPage() throws Exception {
        mockMvc.perform(get("/api/v1/songs/top?sort=unknown&size=500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        verifyNoInteractions(topSongsService);
    }

    @Test
    void topArtists_rejectsNegativePage() throws Exception {
        mockMvc.perform(get("/api/v1/artists/top?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        verifyNoInteractions(topArtistsService);
    }

    @Test
    void search_rejectsBlankAndOversizedQueries() throws Exception {
        mockMvc.perform(get("/api/v1/search").param("q", "  "))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/search").param("q", "x".repeat(101)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(searchService);
    }
}
