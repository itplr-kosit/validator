import React, { useEffect } from "react";
import Codeblock from "../Codeblock";
import ErrorDisplay from "../ErrorDisplay";
import useRequest, { RequestStatus } from "../util/useRequest";

function XmlView({
	endpoint,
	fileName,
}: {
	endpoint: string;
	fileName: string;
}): JSX.Element {
	const { request, data, error, status } = useRequest();

	useEffect(() => {
		request(endpoint, { headers: { "Content-Type": "application/xml" } });
	}, [endpoint, request]);

	return (
		<>
			{status === RequestStatus.Failure && error && (
				<ErrorDisplay title="An error occurred while fetching">
					<Codeblock>{error.message}</Codeblock>
				</ErrorDisplay>
			)}
			{status === RequestStatus.Success && data && (
				<Codeblock download={{ fileName, mime: "application/xml" }} enableCopy>
					{data}
				</Codeblock>
			)}
		</>
	);
}

export default XmlView;
