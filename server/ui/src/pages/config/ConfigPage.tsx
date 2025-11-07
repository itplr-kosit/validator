import React, { JSX } from "react";
import PageLayout from "@site/src/components/PageLayout";
import XmlView from "@site/src/components/XmlView";

function ConfigPage(): JSX.Element {
	return (
		<PageLayout
			title="Validator configuration"
			layoutDescription="The currently loaded validator configuration"
			headline="Validator configuration"
			description="View the currently loaded validator configuration."
		>
			<XmlView endpoint="/server/config" fileName="config.xml" />
		</PageLayout>
	);
}

export default ConfigPage;
