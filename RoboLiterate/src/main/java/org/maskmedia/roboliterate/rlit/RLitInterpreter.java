/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */
package org.maskmedia.roboliterate.rlit;

import android.content.Context;

import org.maskmedia.roboliterate.rlit.instructions.Program;
import java.util.List;
import java.util.Map;

public interface RLitInterpreter {

    Program toProgram(Context context, List<RLitSentence> sentences);

    Map<String, Integer> extractRobotSoundFiles(List<RLitSentence> sentences);
}
