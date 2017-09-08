# JULIE Lab Entity Evaluator

This tool is used for text entity evaluation purposes. To this end, it offers an API to create evaluation data objects programmatically that can then be used to compute Precision, Recall and F-Score metrics.
The tool can also be used from the command line. It accepts the following tab-separated format derived from the BioCreative II Gene Normalization format:

1. column: document ID
2. column: entity ID
3. column: start offset (may be omitted)
4. column: end offset (may be omitted)
5. column: entity string
6. column: tagging system name

The minimal format is just the first two columns.
