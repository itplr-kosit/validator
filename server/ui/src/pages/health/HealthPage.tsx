import React from "react";
import PageLayout from "@site/src/components/PageLayout";
import XmlView from "@site/src/components/XmlView";

function HealthPage(): JSX.Element {
	return (
		<PageLayout
			title="Health information"
			layoutDescription="Health and status information about the system"
			headline="Server health information"
			description="Information about health and status of the running system."
		>
			<XmlView endpoint="/server/health" fileName="health.xml" />
		</PageLayout>
	);
}

export default HealthPage;
