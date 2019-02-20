package io.github.formular_team.formular.ux;

import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.scene.SceneEnvironment;

import java.util.Random;

public class NameProvider implements NameSuggestionProvider {

    private final String[] famousTracks = new String[] { "Pacific Raceways", "Pacific Raceways", "Seatac Offroad Track", "Sonoma Raceway", "Daytona International Speedway", "Thunderhill Raceway", "Tsukuba Circuit", "Sonoma Raceway", "Imola Circuit", "Mount Panorama Circuit", "Virginia International Raceway", "Indianapolis International Raceway", "Watkins Glen International Raceway", "WeatherTech Raceway Laguna Seca", "Monaco GP Circuit", "Interlagos", "Silverstone Circuit", "Circuit de la Sarthe", "Nurburgring Nordschleife", "Suzuka Circuit", "Bremerton Raceway", "Bellevue Circuit" };
    private final String[] firstWord = new String[] { "Spokane's", "Seattle'", "Seattle's", "Seattle's", "Portland's", "The Mazda", "Thunderhill", "Daytona", "Virginia", "Michelin", "Watkins", "Monza", "Silverstone", "Autodrome", "Tsukuba", "Suzuka", "Monaco", "Hallett", "Nurburgring" };
    private final String[] secondWord = new String[] { "Circuit", "Raceway Park", "International Speedway", "Laguna Seca", "Rock Park", "Silverstone", "Circuit", "Speedway", "Raceway", "Monaco", "Hallett", "Nurburgring", "Track", "Track"};

    private Random rand = new Random();

    @Override
    public String create(final SceneEnvironment environment, final Path road) {

        // 50 vs 50 chance, whether famous track name gets returned, or a combo of two words
        if(rand.nextInt(100) < 50)
            return randomFamousTrack();
        else
            return randomWordCombo();
    }

    private String randomFamousTrack() {
        Random rand = new Random();
        return famousTracks[rand.nextInt(famousTracks.length)];
    }

    private String randomWordCombo() {
        Random rand1 = new Random(), rand2 = new Random();
        return firstWord[rand.nextInt(firstWord.length)] + secondWord[rand.nextInt(secondWord.length)];
    }

}
