package revxrsal.commands.annotation;

import revxrsal.commands.autocomplete.AutoCompleter;
import revxrsal.commands.autocomplete.SuggestionProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds tab completion for the command.
 * <p>
 * Each value in {@link #value()} should be separated with a space, and can either be
 * contextual or static:
 * <ul>
 *     <li>
 *         If it is contextual, it should be prefixed with <code>@</code> and
 *         registered with {@link AutoCompleter#registerSuggestion(String, SuggestionProvider)}.
 *     </li>
 *     <li>
 *         If it is static, you can either write up the values right away and separate them
 *         with <code>|</code>, such as <em>1|2|3</em> which will return 1, 2 and 3 when
 *         tab is requested.
 *     </li>
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoComplete {

    /**
     * The tab completion value, by order.
     *
     * @return The tab completion. Check the class documentation
     * for more information.
     */
//    @Pattern("@?([\\w]+)\\|?")
    String value();

}
