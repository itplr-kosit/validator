/* eslint-disable @typescript-eslint/no-var-requires */
/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require("prism-react-renderer/themes/github");
const darkCodeTheme = require("prism-react-renderer/themes/dracula");
const pkg = require("./package.json");

/** @type {import('@docusaurus/types').Config} */
const config = {
	title: "KoSIT Validator Daemon",
	tagline: "Validating any XML",
	url: "https://your-docusaurus-test-site.com",
	baseUrl: "/",
	onBrokenLinks: "throw",
	onBrokenMarkdownLinks: "warn",
	favicon: "img/favicon.svg",
	customFields: {
		// We fake a base endpoint here, so that our proxy works in development mode.
		// It does not seem to work when trying to proxy requests to the root, so we
		// need to create a base path to proxy against
		apiBase: process.env.NODE_ENV === "development" ? "/api" : "/",
	},

	// GitHub pages deployment config.
	// If you aren't using GitHub pages, you don't need these.
	organizationName: "KoSIT", // Usually your GitHub org/user name.
	projectName: "Validator", // Usually your repo name.

	// Even if you don't use internalization, you can use this field to set useful
	// metadata like html lang. For example, if your site is Chinese, you may want
	// to replace "en" with "zh-Hans".
	i18n: {
		defaultLocale: "en",
		locales: ["en"],
	},

	presets: [
		[
			"classic",
			/** @type {import('@docusaurus/preset-classic').Options} */
			({
				docs: {
					sidebarPath: require.resolve("./sidebars.js"),
					// Please change this to your repo.
					// Remove this to remove the "edit this page" links.
					editUrl: "https://github.com/itplr-kosit/validator/server/ui",
				},
				theme: {
					customCss: require.resolve("./src/css/custom.css"),
				},
			}),
		],
	],

	themeConfig:
		/** @type {import('@docusaurus/preset-classic').ThemeConfig} */
		({
			navbar: {
				style: "primary",
				title: "Validator Daemon",
				logo: {
					alt: "KoSIT Validator Daemon",
					src: "img/logo.svg",
				},
				items: [
					{
						type: "doc",
						docId: "api",
						position: "left",
						label: "Documentation",
					},
					{
						to: "config",
						position: "left",
						label: "Validator configuration",
					},
					{
						to: "health",
						position: "left",
						label: "Health information",
					},
				],
			},
			footer: {
				style: "dark",
				links: [
					{
						title: "Docs",
						items: [
							{
								label: "Configuration",
								to: "/docs/configurations",
							},
							{
								label: "API",
								to: "/docs/api",
							},
						],
					},

					{
						title: "Community",
						items: [
							{
								label: "Github",
								href: "https://github.com/itplr-kosit/validator",
							},
							{
								label: "Issues",
								href: "https://github.com/itplr-kosit/validator/issues",
							},
						],
					},
					{
						title: "More",
						items: [
							{
								label: "KoSIT",
								href: "https://www.xoev.de",
							},
							{
								label: "XRechnung",
								href: "https://www.xoev.de/xrechnung-16828",
							},
						],
					},
				],
				copyright: `Copyright © ${new Date().getFullYear()} Koordinierungstelle für IT-Standards (KoSIT)`,
			},
			prism: {
				theme: lightCodeTheme,
				darkTheme: darkCodeTheme,
			},
		}),

	plugins: [
		/** @type {import('@docusaurus/types').PluginModule} */
		(
			// For the development environment to work, we need to proxy all requests
			// that are not meant to fetch static content (js, css, html files), to
			// the backend server. The dev server makes tht a little hard for us, as
			// it does not allow us to just proxy all requests that don't match any
			// static file. That's why we prefix every request with `/api`, and remove
			// it again when forwarding the request. In ptoduction mode, the endpoint
			// will be just `/` (see `config.customFields.apiBase`)
			function proxyPlugin() {
				return {
					name: "custom-docusaurus-plugin",
					configureWebpack() {
						return {
							devServer: {
								proxy: {
									"/api": {
										target: pkg.apiProxy,
										pathRewrite: { "^/api": "" },
									},
								},
							},
						};
					},
				};
			}
		),
	],
};

module.exports = config;
