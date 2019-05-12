[clikt](../../index.md) / [com.github.ajalt.clikt.completion](../index.md) / [CompletionCandidates](./index.md)

# CompletionCandidates

`sealed class CompletionCandidates`

Configurations for generating shell autocomplete suggestions

### Types

| Name | Summary |
|---|---|
| [Fixed](-fixed/index.md) | `data class Fixed : `[`CompletionCandidates`](./index.md)<br>Complete the parameter with a fixed set of string |
| [Hostname](-hostname.md) | `object Hostname : `[`CompletionCandidates`](./index.md)<br>Complete with entries in the system's hostfile |
| [None](-none.md) | `object None : `[`CompletionCandidates`](./index.md)<br>Do not autocomplete this parameter |
| [Path](-path.md) | `object Path : `[`CompletionCandidates`](./index.md)<br>Complete with filesystem paths |
| [Username](-username.md) | `object Username : `[`CompletionCandidates`](./index.md)<br>Complete with usernames from the current system |

### Inheritors

| Name | Summary |
|---|---|
| [Fixed](-fixed/index.md) | `data class Fixed : `[`CompletionCandidates`](./index.md)<br>Complete the parameter with a fixed set of string |
| [Hostname](-hostname.md) | `object Hostname : `[`CompletionCandidates`](./index.md)<br>Complete with entries in the system's hostfile |
| [None](-none.md) | `object None : `[`CompletionCandidates`](./index.md)<br>Do not autocomplete this parameter |
| [Path](-path.md) | `object Path : `[`CompletionCandidates`](./index.md)<br>Complete with filesystem paths |
| [Username](-username.md) | `object Username : `[`CompletionCandidates`](./index.md)<br>Complete with usernames from the current system |
