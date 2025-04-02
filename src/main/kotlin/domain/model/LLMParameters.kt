package domain.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Represents parameters for LLM requests based on Ollama Modelfile documentation
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LLMParameters(
    /**
     * The format to return a response in (json or a JSON schema)
     */
    val format: String? = null,

    /**
     * Additional model parameters listed in the documentation
     */
    val options: ModelOptions? = null,

    /**
     * System message (overrides what is defined in the Modelfile)
     */
    val system: String? = null,

    /**
     * The prompt template to use (overrides what is defined in the Modelfile)
     */
    val template: String? = null,

    /**
     * If false, the response will be returned as a single response object,
     * rather than a stream of objects
     */
    val stream: Boolean? = null,

    /**
     * If true, no formatting will be applied to the prompt.
     * Use when specifying a full templated prompt in your request to the API
     */
    val raw: Boolean? = null,

    /**
     * Controls how long the model will stay loaded into memory following the request
     * Default: "5m"
     */
    val keepAlive: String? = null,

    /**
     * @Deprecated The context parameter returned from a previous request to /generate
     * This can be used to keep a short conversational memory
     */
    @Deprecated("Use newer conversation handling methods instead")
    val context: String? = null
)

/**
 * Model options parameters as specified in the Modelfile documentation
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ModelOptions(
    /**
     * Enable Mirostat sampling for controlling perplexity
     * Default: 0 (disabled)
     * Values: 0 = disabled, 1 = Mirostat, 2 = Mirostat 2.0
     */
    val mirostat: Int? = null,

    /**
     * Influences how quickly the algorithm responds to feedback from the generated text
     * A lower learning rate will result in slower adjustments, while a higher learning rate
     * will make the algorithm more responsive
     * Default: 0.1
     */
    val mirostatEta: Float? = null,

    /**
     * Controls the balance between coherence and diversity of the output
     * A lower value will result in more focused and coherent text
     * Default: 5.0
     */
    val mirostatTau: Float? = null,

    /**
     * Sets the size of the context window used to generate the next token
     * Default: 2048
     */
    val numCtx: Int? = null,

    /**
     * Sets how far back for the model to look back to prevent repetition
     * Default: 64
     * Values: 0 = disabled, -1 = num_ctx
     */
    val repeatLastN: Int? = null,

    /**
     * Sets how strongly to penalize repetitions
     * A higher value (e.g., 1.5) will penalize repetitions more strongly,
     * while a lower value (e.g., 0.9) will be more lenient
     * Default: 1.1
     */
    val repeatPenalty: Float? = null,

    /**
     * The temperature of the model
     * Increasing the temperature will make the model answer more creatively
     * Default: 0.8
     */
    val temperature: Float? = null,

    /**
     * Sets the random number seed to use for generation
     * Setting this to a specific number will make the model generate the same text
     * for the same prompt
     * Default: 0
     */
    val seed: Int? = null,

    /**
     * Sets the stop sequences to use
     * When this pattern is encountered the LLM will stop generating text and return
     * Multiple stop patterns may be provided as a list
     */
    val stop: List<String>? = null,

    /**
     * Maximum number of tokens to predict when generating text
     * Default: -1 (infinite generation)
     */
    val numPredict: Int? = null,

    /**
     * Reduces the probability of generating nonsense
     * A higher value (e.g. 100) will give more diverse answers,
     * while a lower value (e.g. 10) will be more conservative
     * Default: 40
     */
    val topK: Int? = null,

    /**
     * Works together with top-k
     * A higher value (e.g., 0.95) will lead to more diverse text,
     * while a lower value (e.g., 0.5) will generate more focused and conservative text
     * Default: 0.9
     */
    val topP: Float? = null,

    /**
     * Alternative to the top_p, and aims to ensure a balance of quality and variety
     * Represents the minimum probability for a token to be considered, relative to
     * the probability of the most likely token
     * Default: 0.0
     */
    val minP: Float? = null
)
