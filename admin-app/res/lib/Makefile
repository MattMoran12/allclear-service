scripts/common.js: scripts/common/template.js \
			scripts/common/tabTemplate.js \
			scripts/common/listTemplate.js \
			scripts/common/editTemplate.js \
			scripts/common/history.js \
			scripts/common/json.js \
			scripts/common/viewTemplate.js \
			scripts/common/progressBar.js \
			scripts/common/keyCodes.js \
			scripts/common/bulkAnswers.js \
			scripts/common/dropdown.js \
			scripts/common/datePicker.js \
			scripts/common/login.js \
			scripts/common/metadata.js \
			scripts/common/permissions.js \
			scripts/common/tagger.js \
			scripts/common/timePicker.js \
			scripts/common/timestampPicker.js
	java -jar lib/compiler.jar --js_output_file=$@ $^

clean:
	rm scripts/common.js

.PHONY: clean
