package io.github.formular_team.formular.ux;

import android.content.Context;
import io.github.formular_team.formular.R;
import io.github.formular_team.formular.math.Path;
import io.github.formular_team.formular.scene.SceneEnvironment;

import java.util.Random;

public class NameProvider implements NameSuggestionProvider {

    private final String[] firstWord;
    private final String[] secondWord;
    private final String[] famousTrackNames;

    private Random rand = new Random();


    public NameProvider(Context context) {
        firstWord = context.getResources().getStringArray(R.array.first_word);
        secondWord = context.getResources().getStringArray(R.array.second_word);
        famousTrackNames = context.getResources().getStringArray(R.array.famous_track_names);
    }

    @Override
    public String create(final SceneEnvironment environment, final Path road) {
        // 75% chance color and second word combined.  20% random first and random second word are combined. 5% a famous race track name will be spit out.
        float lotteryNumber = rand.nextFloat();

        if(lotteryNumber < 0.50f)
            return new ColorName(environment.foreground()).getName() + secondWord[rand.nextInt(secondWord.length)];

        else if(lotteryNumber < 0.75f)
            return new ColorName(environment.background()).getName() + secondWord[rand.nextInt(secondWord.length)];

        else if(lotteryNumber < 0.95f)
            return firstWord[rand.nextInt(firstWord.length)] + secondWord[rand.nextInt(secondWord.length)];

        else
            return famousTrackNames[rand.nextInt(famousTrackNames.length)];
    }
}
